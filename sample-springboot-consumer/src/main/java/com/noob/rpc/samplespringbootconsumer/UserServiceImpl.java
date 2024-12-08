package com.noob.rpc.samplespringbootconsumer;

import com.noob.rpc.entity.User;
import com.noob.rpc.noobrpcspringbootstarter.annotation.RpcReference;
import com.noob.rpc.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户操作
 */
@Service
public class UserServiceImpl {
    @RpcReference
    private UserService userService;

    public void getName(){
        User user = new User("我的名字叫lxw");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}
