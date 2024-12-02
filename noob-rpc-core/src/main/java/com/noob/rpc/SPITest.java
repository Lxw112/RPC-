package com.noob.rpc;

import com.noob.rpc.serializer.Serializer;

import java.util.ServiceLoader;

/**
 * SPI机制测试
 */
public class SPITest {
    public static void main(String[] args) {
        // 方式1：系统实现
        Serializer serializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer service : serviceLoader) {
            serializer = service;
        }
        System.out.println(serializer);
    }
}

// 通过遍历可以获取到文件中定义的所有类，按需选择即可
