package com.noob.rpc;

import com.noob.rpc.proxy.ServiceProxyFactory;
import com.noob.rpc.service.UserService;

/**
 * 消费者调用请求
 */
public class CoreConsumerMockSample {
    public static void main(String[] args) {
        // 动态代理模式
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        // 调用
        short number = userService.getNumber();
        System.out.println(number);
    }
}
