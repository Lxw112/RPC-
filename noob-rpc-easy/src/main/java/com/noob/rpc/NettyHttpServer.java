package com.noob.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        // 创建事件循环组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 用于接受客户端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 用于处理客户端的I/O操作

        try {
            // 创建服务器启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                                      @Override
                                      protected void initChannel(Channel ch) throws Exception {
                                          // 管道配置：添加 HTTP 编解码器和处理请求的自定义处理器
                                          ChannelPipeline pipeline = ch.pipeline();
                                          pipeline.addLast(new HttpServerCodec()); // 编解码器
                                          pipeline.addLast(new HttpObjectAggregator(65536)); // 聚合 HTTP 请求
                                          pipeline.addLast(new HttpRequestHandler<FullHttpRequest>());
                                      }
                                  }
                    );

            // 绑定服务器端口
            ChannelFuture future = serverBootstrap.bind(port).sync();
            if (future.isSuccess()) {
                System.out.println("Server is now listening on port " + port);
            }

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 关闭所有资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
