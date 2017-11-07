package com.zhizus.forest.support;


import com.google.common.collect.Maps;
import com.zhizus.forest.ForestContext;
import com.zhizus.forest.common.codec.Request;
import com.zhizus.forest.common.interceptor.AbstractInvokerInterceptor;
import com.zhizus.forest.common.util.ForestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Dempe on 2016/12/7.
 */
public class MetricInterceptor extends AbstractInvokerInterceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MetricInterceptor.class);

    private final static Map<String, Metric> metricsMap = Maps.newConcurrentMap();
    private final static String BEG_TIME = "begTime";

    /**
     * scheduleAtFixedRate  是以上一个任务开始的时间计时，period时间过去后，检测上一个任务是否执行完毕，
     * 如果上一个任务执行完毕，则当前任务立即执行，
     * 如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行。
     */
    final static ScheduledFuture<?> scheduledFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
            for (Map.Entry<String, Metric> stringMetricEntry : metricsMap.entrySet()) {
                Metric value = stringMetricEntry.getValue();
                LOGGER.info(" methodName:{}, current tps:{}, avgTime:{}, maxTime:{}, minTime:{} ",
                        stringMetricEntry.getKey(), value.getAndSet(), value.totalTime / 60, value.maxTime, value.minTime);
            }
        }
    }, 0, 1, TimeUnit.SECONDS);

    @Override
    public boolean beforeInvoke(Object target, Method method, Object... args) {
        ForestContext.putAttr(BEG_TIME, String.valueOf(System.currentTimeMillis()));
        return true;
    }


    @Override
    public boolean afterInvoke(Object target, Method method, Object result) {
        Long beginTime = Long.valueOf(ForestContext.getAttr(BEG_TIME));
        long exeTime = System.currentTimeMillis() - beginTime;
        Object content = ForestContext.getMessage().getContent();
        if (!(content instanceof Request)) {
            return true;
        }
        String key = ForestUtil.buildUri(((Request) content).getServiceName(), ((Request) content).getMethodName());
        Metric metric = metricsMap.get(key);
        if (metric == null) {
            synchronized (this) {
                metric = metricsMap.get(key);
                if (metric == null) {
                    metric = new Metric();
                    metricsMap.put(key, metric);
                }
            }
        }
        metric.incrementAndGetTPS();
        metric.exeTime(exeTime);
        return true;
    }

    class Metric {
        private int minTime;
        private int maxTime;
        private int totalTime;
        private AtomicLong tps = new AtomicLong(0);

        public long incrementAndGetTPS() {
            return tps.incrementAndGet();
        }


        public long getAndSet() {
            totalTime = 0;
            return tps.getAndSet(0);
        }

        public synchronized void exeTime(long currentTime) {
            if (currentTime < minTime || minTime == 0) {
                minTime = (int) currentTime;
            }
            if (currentTime > maxTime) {
                maxTime = (int) currentTime;
            }
            totalTime += currentTime;
        }

        public void setTotalTime(int totalTime) {
            this.totalTime = totalTime;
        }
    }

}

