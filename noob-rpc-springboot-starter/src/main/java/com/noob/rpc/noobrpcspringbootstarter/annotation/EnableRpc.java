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
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class,RpcConsumerBootstrap.class })
public @interface EnableRpc {
    /**
     * 需要启动 server
     * @return
     */
    boolean needServer() default true;
}
