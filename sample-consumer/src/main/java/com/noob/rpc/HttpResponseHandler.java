package com.noob.rpc;

import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.serializer.JdkSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    //                        序号        用来接收结果
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        JdkSerializer jdkSerializer = new JdkSerializer();
        ByteBuf content = msg.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        RpcResponse response = jdkSerializer.deserialize(bytes, RpcResponse.class);
        Promise<Object> promise = PROMISES.remove(response.getId());
        if (promise != null) {
            Object data = response.getData();
            if (data ==null){
                promise.setFailure(new Throwable("响应结果为null"));
            }else {
                promise.setSuccess(data);
            }
        }
    }
}
