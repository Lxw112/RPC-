package com.noob.rpc;

import com.fasterxml.jackson.databind.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.util.List;


public class RequestMessageCodec extends MessageToMessageCodec<ByteBuf,RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest request, List<Object> list) throws Exception {
        System.out.println("准备编码");
        ByteBuf out = ctx.alloc().buffer(1024);
        RpcJsonSerializer rpcJsonSerializer = new RpcJsonSerializer();
        byte[] bytes = rpcJsonSerializer.serialize(request);
        ByteBuf buf = ctx.alloc().buffer(1024);
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:8080", buf.writeBytes(bytes));
        byte[] bytes1 = new byte[0];
        try {
            bytes1 = rpcJsonSerializer.serialize(httpRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("编码完成");
        ctx.writeAndFlush(out.writeBytes(bytes1));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RpcJsonSerializer rpcJsonSerializer = new RpcJsonSerializer();
        int length = in.readableBytes();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        RpcResponse rpcResponse =null;
        FullHttpResponse response;
        try {
            response = rpcJsonSerializer.deserialize(bytes, DefaultFullHttpResponse.class);
            ByteBuf content = response.content();
            int contentLength = content.readableBytes();
            byte[] bytes1 = new byte[contentLength];
            content.readBytes(bytes1);
            rpcResponse = rpcJsonSerializer.deserialize(bytes1, RpcResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.add(rpcResponse);
    }

    }

