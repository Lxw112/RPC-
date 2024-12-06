package com.noob.rpc;

import com.noob.rpc.bootstrap.ConsumerBootstrap;
import com.noob.rpc.entity.User;
import com.noob.rpc.proxy.ServiceProxyFactory;
import com.noob.rpc.service.UserService;

/**
 * 服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 服务提供者初始化
        ConsumerBootstrap.init();

        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User("Tom");

        // 调用
        User newUser = userService.getUser(user);

        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}