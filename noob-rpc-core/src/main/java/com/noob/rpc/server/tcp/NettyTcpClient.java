package com.noob.rpc.server.tcp;

import com.noob.rpc.RpcApplication;
import com.noob.rpc.config.RpcConfig;
import com.noob.rpc.handler.TcpResponseServerHandler;
import com.noob.rpc.protocol.ProtocolMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;

public class NettyTcpClient {
    public static void main(String[] args) {
        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        getChannel(rpcConfig.getServerHost(),rpcConfig.getServerPort());
    }
    private static Channel channel = null;
    private static final Object LOCK =new Object();

    //获取唯一的channel对象
    public static Channel getChannel(String host,int port){
        if (channel != null){
            return channel;
        }
        synchronized (LOCK){
            if (channel != null){
                return channel;
            }
            initChannel(host,port);
            return channel;
        }
    }
    private static void initChannel(String host,int port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,0,4,0,4));
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new ProtocolMessageCodec());
                            ch.pipeline().addLast(new TcpResponseServerHandler());
                        }
                    });
            channel = bootstrap.connect(host, port).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
