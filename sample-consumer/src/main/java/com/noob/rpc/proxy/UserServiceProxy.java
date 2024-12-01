package com.noob.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.noob.rpc.HttpResponseHandler;
import com.noob.rpc.entity.User;
import com.noob.rpc.pojo.RpcRequest;
import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.serializer.JdkSerializer;
import com.noob.rpc.serializer.Serializer;
import com.noob.rpc.service.UserService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;

public class UserServiceProxy implements UserService {
    private static Channel channel = null;
    private static final Object LOCK =new Object();

    //获取唯一的channel对象
    public static Channel getChannel(){
        if (channel != null){
            return channel;
        }
        synchronized (LOCK){
            if (channel != null){
                return channel;
            }
            initChannel();
            return channel;
        }
    }
    @Override
    public User getUser(User user) {
        // 指定序列化器
        final Serializer serializer = new JdkSerializer();
        System.out.println("静态代理");

        // 构造RpcRequest对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(UserService.class.getName());
        rpcRequest.setMethodName("getUser");
        rpcRequest.setParameterTypes(new Class[]{User.class});
        rpcRequest.setArgs(new Object[]{user});
        System.out.println(rpcRequest);
        // 序列化
        byte[] bytes = null;
        try {
            bytes = serializer.serialize(rpcRequest);
            //发送请求
            HttpResponse response = HttpRequest.post("http://localhost:8080")
                    .contentType("application/octet-stream")  // 设置为二进制流
                    .body(bytes)
                    .execute();
            //获得请求结果对象
            byte[] resBytes = response.bodyBytes();
            //反序列化为对象
            RpcResponse result = serializer.deserialize(resBytes, RpcResponse.class);
            return (User) result.getData();
            // 发送请求
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //        initChannel();
//        // 发送请求
//        System.out.println("准备发送请求");
//        getChannel().writeAndFlush(rpcRequest);
//        //准备一个promise对象，来接收结果                  指定promise对象异步接收结果的线程
//        DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
//        //等待promise的结果
//        try {
//            promise.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        if (promise.isSuccess()){
//            //调用正常
//            return (User) promise.getNow();
//        }else {
//            //调用失败
//            throw new RuntimeException(promise.cause());
    //
//        }
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new RequestMessageCodec());
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpResponseHandler());
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    });
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
