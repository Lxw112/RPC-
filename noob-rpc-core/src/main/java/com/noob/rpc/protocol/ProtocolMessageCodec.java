package com.noob.rpc.protocol;

import com.noob.rpc.model.RpcRequest;
import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.serializer.Serializer;
import com.noob.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * 协议消息编解码器
 */
public class ProtocolMessageCodec extends ByteToMessageCodec<ProtocolMessage<?>> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProtocolMessage<?> msg, ByteBuf out) throws Exception {
        System.out.println("--------------准备编码");
        if (msg == null || msg.getHeader() == null){
            out.writeBytes(new byte[]{});
        }
        ProtocolMessage.Header header = msg.getHeader();
        //依次向缓冲区写入字节
        out.writeByte(header.getMagic());
        out.writeByte(header.getVersion());
        out.writeByte(header.getSerializer());
        out.writeByte(header.getType());
        out.writeByte(header.getStatus());
        out.writeLong(header.getRequestId());
        //获取序列化器
        ProtocolMessageSerializerEnum serializerEnum =
                ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(msg.getBody());
        //写入body长度和数据
        out.writeInt(bodyBytes.length);
        out.writeBytes(bodyBytes);
        System.out.println("---------编码完成");
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        System.out.println("------------------------准备解码");
        //分别从指定位置读出buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = in.readByte();
        //校验魔数
        if (magic != ProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException("消息 magic 非法");
        }
        header.setMagic(magic);
        header.setVersion(in.readByte());
        header.setSerializer(in.readByte());
        header.setType(in.readByte());
        header.setStatus(in.readByte());
        header.setRequestId(in.readLong());
        header.setBodyLength(in.readInt());
        //解决粘包问题，只读指定长度的数据
        byte[] bodyBytes = new byte[header.getBodyLength()];
        in.readBytes(bodyBytes,0,header.getBodyLength());
        //解析消息体
        ProtocolMessageSerializerEnum serializerEnum =
                ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                list.add(new ProtocolMessage<>(header,request));
                System.out.println("---------解码完成");
                break;
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                list.add(new ProtocolMessage<>(header,response));
                System.out.println("---------解码完成");
                break;
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该消息类型");
        }
    }
}
