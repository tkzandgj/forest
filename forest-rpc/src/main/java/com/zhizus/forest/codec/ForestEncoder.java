package com.zhizus.forest.codec;

import com.zhizus.forest.common.CompressType;
import com.zhizus.forest.common.EventType;
import com.zhizus.forest.common.SerializeType;
import com.zhizus.forest.common.codec.Header;
import com.zhizus.forest.common.codec.Message;
import com.zhizus.forest.common.codec.compress.Compress;
import com.zhizus.forest.common.codec.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by Dempe on 2016/12/7.
 */
public class ForestEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        Header header = message.getHeader();
        // 将header的数据写入到ByteBuf中
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getExtend());
        byteBuf.writeLong(header.getMessageID());
        Object content = message.getContent();
        // 心跳消息,没有body
        if (content == null && EventType.typeofHeartBeat(header.getExtend())) {
            byteBuf.writeInt(0);
            return;
        }
        Serialization serialization = SerializeType.getSerializationByExtend(header.getExtend());
        Compress compress = CompressType.getCompressTypeByValueByExtend(header.getExtend());
        byte[] payload = compress.compress(serialization.serialize(content));
        byteBuf.writeInt(payload.length);
        byteBuf.writeBytes(payload);
    }
}
