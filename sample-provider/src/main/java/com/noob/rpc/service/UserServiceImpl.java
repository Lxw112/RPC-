package com.noob.rpc.service;

import com.noob.rpc.entity.User;
import com.noob.rpc.service.UserService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * 用户接口实现类
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名："+user.getName());
        return user;
    }
}
