package com.noob.rpc;

import com.fasterxml.jackson.databind.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class ResponseMessageCodec extends MessageToMessageCodec<ByteBuf,RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse response, List<Object> list) throws Exception {
        ByteBuf out = ctx.alloc().buffer(1024);
        RpcJsonSerializer rpcJsonSerializer = new RpcJsonSerializer();
        byte[] bytes = rpcJsonSerializer.serialize(response);
        ByteBuf buf = ctx.alloc().buffer(1024);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,buf.writeBytes(bytes));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        byte[] bytes1 = rpcJsonSerializer.serialize(httpResponse);
        ctx.writeAndFlush(out.writeBytes(bytes1));

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RpcJsonSerializer rpcJsonSerializer = new RpcJsonSerializer();
        int length = in.readableBytes();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        RpcRequest rpcRequest =null;
        FullHttpRequest request;
        try {
            log.debug("开始解码..{}", bytes);
            request = rpcJsonSerializer.deserialize(bytes, DefaultFullHttpRequest.class);
            ByteBuf content = request.content();
            System.out.println("请求是"+content);
            int contentLength = content.readableBytes();
            byte[] bytes1 = new byte[contentLength];
            content.readBytes(bytes1);
            log.debug("再次解码..{}",bytes1);
             rpcRequest = rpcJsonSerializer.deserialize(bytes1, RpcRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.add(rpcRequest);
    }
}
