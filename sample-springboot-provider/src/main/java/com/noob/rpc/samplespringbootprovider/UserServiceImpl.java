package com.noob.rpc.samplespringbootprovider;

import com.noob.rpc.entity.User;
import com.noob.rpc.noobrpcspringbootstarter.annotation.RpcService;
import com.noob.rpc.service.UserService;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
