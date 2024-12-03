package com.noob.rpc.handler;

import com.noob.rpc.registry.LocalRegistry;
import com.noob.rpc.RpcApplication;
import com.noob.rpc.model.RpcRequest;
import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.serializer.Serializer;
import com.noob.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.lang.reflect.Method;

public class HttpRequestHandler<F> extends SimpleChannelInboundHandler<FullHttpRequest> {
    /**
     * 主要是处理http请求和响应数据
     */
    @Override

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 硬编码方式：指定序列化器
        //final Serializer serializer = new JdkSerializer();


        // 方式2：动态获取序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());


        // 记录日志
        System.out.println("接收到请求: " + request.headers() + " " + request.getUri());

        // 拦截请求，检查请求体是否为空
        ByteBuf content = request.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        if (bytes.length == 0) {
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setMessage("Empty request body");
            doResponse(ctx, rpcResponse, serializer);
            return;
        }

        // 反序列化，构造RpcRequest对象准备发送至provider0
        RpcRequest rpcRequest = null;

        try {
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("转换完成的："+rpcRequest);
        } catch (Exception e) {
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setMessage("Deserialization error: " + e.getMessage());
            doResponse(ctx, rpcResponse, serializer);
            return;
        }

        // 构造响应结果对象
        RpcResponse rpcResponse = new RpcResponse();
        if (rpcRequest == null) {
            rpcResponse.setMessage("rpcRequest is null");
            doResponse(ctx, rpcResponse, serializer);
            return;
        }

        // 利用反射获取对应的实现类，调用方法获取数据
        try {
            // 获取对象
            Class<?> aClass = LocalRegistry.get(rpcRequest.getServiceName());//这个才是实现类
            // 获取对象对应的方法
            Method method = aClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            // 实例化
            Object o = aClass.newInstance();
            // 调用方法得到返回值
            Object res = method.invoke(o, rpcRequest.getArgs());
            // 给response赋值
            rpcResponse.setData(res);
            rpcResponse.setDataType(method.getReturnType());
            rpcResponse.setMessage("ok");

            // 响应结果
            doResponse(ctx, rpcResponse, serializer);
        } catch (Exception e) {
            rpcResponse.setMessage("Error invoking method: " + e.getMessage());
            doResponse(ctx, rpcResponse, serializer);
            throw new RuntimeException(e);
        }
    }

    // 响应客户端的结果
    public void doResponse(ChannelHandlerContext ctx, RpcResponse rpcResponse, Serializer serializer) {
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set("Content-Type", "application/octet-stream"); // 响应内容为二进制流
        try {
            // 将 rpcResponse 序列化成字节数组
            byte[] serialize = serializer.serialize(rpcResponse);

            // 使用 Unpooled 将字节数组包装成 ByteBuf 对象
            ByteBuf byteBuf = Unpooled.wrappedBuffer(serialize);
            response.headers().set("Content-Length", byteBuf.readableBytes());

            // 使用 ctx.writeAndFlush() 将 ByteBuf 写入并刷新到客户端
            ctx.writeAndFlush(byteBuf);

            // 调试日志
            System.out.println("Sending response: " + rpcResponse.getMessage());
        } catch (IOException e) {
            rpcResponse.setMessage("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
