package com.noob.rpc;

import com.noob.rpc.server.HttpServer;
import com.noob.rpc.server.NettyHttpServer;
import com.noob.rpc.service.UserService;
import com.noob.rpc.service.UserServiceImpl;

/**
 * 服务提供者启动类，通过main方法编写提供服务的代码
 */
public class EasyProviderSample {
    public static void main(String[] args) {
        // 提供服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动web服务
        HttpServer httpServer = new NettyHttpServer();
        httpServer.doStart(8080);
    }
}
