package com.noob.rpc.constant;

/**
 * RPC 相关常量
 */
public interface RpcConstant {

    /**
     * 默认配置文件加载前缀
     */
    String DEFAULT_CONFIG_PREFIX = "rpc";

    /**
     * 默认服务版本
     */
    String DEFAULT_SERVICE_VERSION = "1.0";

}

// 默认配置文件加载前缀：可设定读取到类似下列配置
//rpc.name=noob-rpc
//rpc.version=2.0
//rpc.serverPort=8080

