package com.noob.rpc.noobrpcspringbootstarter.bootstrap;

import com.noob.rpc.RpcApplication;
import com.noob.rpc.config.RpcConfig;
import com.noob.rpc.noobrpcspringbootstarter.annotation.EnableRpc;
import com.noob.rpc.server.tcp.NettyTcpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Rpc 框架启动类
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化执行，初始化 Rpc 框架
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取 EnableRpc 注解的属性值
        boolean needServer = (boolean)importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())
                .get("needServer");
        //Rpc框架初始化（配置和注册中心）
        RpcApplication.init();
        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        //启动服务器

        if (needServer) {
            new Thread(()->{
                NettyTcpServer nettyTcpServer = new NettyTcpServer();
                nettyTcpServer.doStart(rpcConfig.getServerPort());
            }).start();
        }else {
            log.info("不启动 server");
        }
    }
}
