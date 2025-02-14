package com.noob.rpc.noobrpcspringbootstarter.annotation;

import com.noob.rpc.noobrpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.noob.rpc.noobrpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.noob.rpc.noobrpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用Rpc注解
 */
@Target({ElementType.TYPE})//TYPE: 表示该注解可以应用于类、接口、枚举等类型的声明
@Retention(RetentionPolicy.RUNTIME)//RUNTIME: 注解会在编译后和运行时都存在，可以通过反射访问。这是大多数需要在运行时被程序读取或处理的注解类型，如 @Entity、@Transactional
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class,RpcConsumerBootstrap.class })
public @interface EnableRpc {
    /**
     * 需要启动 server
     * @return
     */
    boolean needServer() default true;
}
