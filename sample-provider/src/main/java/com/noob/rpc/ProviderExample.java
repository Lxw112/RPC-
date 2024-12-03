package com.noob.rpc;

import com.noob.rpc.bootstrap.ProviderBootstrap;
import com.noob.rpc.model.ServiceRegisterInfo;
import com.noob.rpc.service.UserService;
import com.noob.rpc.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例
 *
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}