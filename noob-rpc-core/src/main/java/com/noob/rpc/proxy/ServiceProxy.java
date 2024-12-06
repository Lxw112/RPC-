package com.noob.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.noob.rpc.RpcApplication;
import com.noob.rpc.config.RpcConfig;
import com.noob.rpc.constant.RpcConstant;
import com.noob.rpc.handler.TcpResponseServerHandler;
import com.noob.rpc.model.RpcRequest;
import com.noob.rpc.model.ServiceMetaInfo;
import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.protocol.*;
import com.noob.rpc.registry.Registry;
import com.noob.rpc.registry.RegistryFactory;
import com.noob.rpc.serializer.Serializer;
import com.noob.rpc.serializer.SerializerFactory;
import com.noob.rpc.server.tcp.NettyTcpClient;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 自定义服务代理类：当用户调用某个接口方法时，会改为调用invoke方法，在invoke方法中获取到要调用的方法信息、参数列表等，通过这些参数构造请求参数进而完成调用
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 硬编码方式：指定序列化器
        //final Serializer serializer = new JdkSerializer();


        // 方式2：动态获取序列化器
        //final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        protocolMessage.setBody(rpcRequest);
        protocolMessage.setHeader(header);

            // 序列化
            //byte[] bodyBytes = serializer.serialize(protocolMessage);

            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());

            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            // 获取到第一个服务信息
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            // 发送请求
            Channel channel = NettyTcpClient.getChannel(selectedServiceMetaInfo.getServiceHost(), selectedServiceMetaInfo.getServicePort());
        System.out.println("------------准备发送请求");
            channel.writeAndFlush(protocolMessage);
            //3.准备一个promise对象，来接收结果                  指定promise对象异步接收结果的线程
            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
            TcpResponseServerHandler.PROMISES.put(header.getRequestId(), promise);

            /*promise.addListener(future -> {
                //线程
            });*/
            //4.等待promise的结果
            promise.await();
            if (promise.isSuccess()) {
                //调用正常
                return promise.getNow();
            } else {
                //调用失败
                throw new RuntimeException(promise.cause());
            }
    }
}
