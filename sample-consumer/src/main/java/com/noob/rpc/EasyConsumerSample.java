package com.noob.rpc;

import com.noob.rpc.entity.User;
import com.noob.rpc.proxy.ServiceProxyFactory;
import com.noob.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EasyConsumerSample {
    public static void main(String[] args) {
        User user = new User("noob");
            // 调用
            UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User newUser = null;
        try {
            newUser = userService.getUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if(newUser != null) {
                System.out.println(newUser.getName() + Thread.currentThread().getName());
            }else {
                System.out.println("user == null");
            }
    }
}
