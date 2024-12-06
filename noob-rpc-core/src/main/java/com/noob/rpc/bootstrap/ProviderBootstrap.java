package com.noob.rpc.bootstrap;

import com.noob.rpc.RpcApplication;
import com.noob.rpc.config.RegistryConfig;
import com.noob.rpc.config.RpcConfig;
import com.noob.rpc.model.ServiceMetaInfo;
import com.noob.rpc.model.ServiceRegisterInfo;
import com.noob.rpc.registry.LocalRegistry;
import com.noob.rpc.registry.Registry;
import com.noob.rpc.registry.RegistryFactory;
import com.noob.rpc.server.NettyHttpServer;
import com.noob.rpc.server.tcp.NettyTcpServer;

import java.util.List;

/**
 * 服务提供者启动类（初始化）
 *
 */
public class ProviderBootstrap {

    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 注册服务
        //NettyHttpServer nettyHttpServer = new NettyHttpServer();
        NettyTcpServer nettyTcpServer = new NettyTcpServer();
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        // 启动服务器
        nettyTcpServer.doStart(rpcConfig.getServerPort());
    }
}