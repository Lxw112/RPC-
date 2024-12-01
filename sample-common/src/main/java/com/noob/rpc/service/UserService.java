package com.noob.rpc.service;

import com.noob.rpc.entity.User;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户信息
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * mock测试
     */
    default short getNumber(){
        return -1;
    }
}
