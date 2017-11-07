package com.zhizus.forest.codec;

import com.zhizus.forest.common.*;
import com.zhizus.forest.common.codec.Header;
import com.zhizus.forest.common.codec.Message;
import com.zhizus.forest.common.codec.compress.Compress;
import com.zhizus.forest.common.codec.serialize.Serialization;
import com.zhizus.forest.common.exception.ForestFrameworkException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by Dempe on 2016/12/7.
 */
public class ForestDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        /**
         * 规定Header_size的大小
         */
        if (byteBuf.readableBytes() < Constants.HEADER_SIZE) {
            return;
        }
        /**
         * 标记readerIndex的位置
         */
        byteBuf.markReaderIndex();

        short magic = byteBuf.readShort();
        if (magic != Constants.MAGIC) {
            /**
             * 恢复readerIndex的位置
             */
            byteBuf.resetReaderIndex();
            throw new ForestFrameworkException("ForestDecoder transport header not support, type: " + magic);
        }
        byte version = byteBuf.readByte();
        byte extend = byteBuf.readByte();
        long messageID = byteBuf.readLong();
        int size = byteBuf.readInt();
        Object req = null;
        if (!(size == 0 && EventType.typeofHeartBeat(extend))) {
            if (byteBuf.readableBytes() < size) {
                byteBuf.resetReaderIndex();
                return;
            }
            // TODO 限制最大包长
            byte[] payload = new byte[size];
            /**
             * 将ByteBuf中的内容读取到payload字节数组中
             */
            byteBuf.readBytes(payload);
            Serialization serialization = SerializeType.getSerializationByExtend(extend);
            Compress compress = CompressType.getCompressTypeByValueByExtend(extend);
            req = serialization.deserialize(compress.unCompress(payload), MessageType.getMessageTypeByExtend(extend));
        }
        Header header = new Header(magic, version, extend, messageID, size);
        Message message = new Message(header, req);
        list.add(message);
    }
}
