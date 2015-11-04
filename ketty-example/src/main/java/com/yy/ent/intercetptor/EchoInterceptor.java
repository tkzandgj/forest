package com.yy.ent.intercetptor;


import com.yy.ent.mvc.anno.Around;
import com.yy.ent.srv.http.HttpServerContext;
import com.yy.ent.srv.interceptor.BaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2015/11/3
 * Time: 16:23
 * To change this template use File | Settings | File Templates.
 */
@Around
public class EchoInterceptor extends BaseInterceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(EchoInterceptor.class);


    @Override
    public boolean before() {
        LOGGER.info("==============EchoInterceptor before=========context:{}", HttpServerContext.getReqCxt());
        return true;
    }

    @Override
    public boolean after() {
        LOGGER.info("==============EchoInterceptor after=========context:{}", HttpServerContext.getReqCxt());
        return true;
    }
}
