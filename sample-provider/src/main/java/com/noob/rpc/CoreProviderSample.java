package com.noob.rpc;

import com.noob.rpc.model.ServiceMetaInfo;
import com.noob.rpc.registry.Registry;
import com.noob.rpc.registry.RegistryFactory;
import com.noob.rpc.server.HttpServer;
import com.noob.rpc.server.NettyHttpServer;
import com.noob.rpc.service.UserService;
import com.noob.rpc.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;


public class CoreProviderSample {
    public static void main(String[] args) throws Exception{
        //框架初始化
        RpcApplication.init();
        System.out.println("Hello world!");// 提供服务
        //从工厂获取
        Registry instance = RegistryFactory.getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("127.0.0.1");
        serviceMetaInfo.setServicePort(8081);
        instance.register(serviceMetaInfo);
        // 启动web服务
        HttpServer httpServer = new NettyHttpServer();
        // 启动web服务（从RPC框架中的全局配置中获取端口）
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
