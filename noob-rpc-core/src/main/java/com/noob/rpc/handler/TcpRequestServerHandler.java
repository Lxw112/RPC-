package com.noob.rpc.handler;

import com.noob.rpc.model.RpcRequest;
import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.protocol.*;
import com.noob.rpc.registry.LocalRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class TcpRequestServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<?>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<?> protocolMessage) throws Exception {
        System.out.println("-----------------------收到解码后的请求");
        RpcRequest rpcRequest = (RpcRequest) protocolMessage.getBody();
        //处理请求
        //构造响应结果对象
        RpcResponse rpcResponse = new RpcResponse();
        try {
            //获取要调用的服务实现类，通过反射调用
            Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
            Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
            //封装返回结果
            rpcResponse.setData(result);
            rpcResponse.setDataType(method.getReturnType());
            rpcResponse.setMessage("ok");
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setMessage(e.getMessage());
            rpcResponse.setException(e);
        }
        //发送响应
        ProtocolMessage.Header header = protocolMessage.getHeader();
        header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(protocolMessage.getHeader().getRequestId());
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
        System.out.println("--------------------准备发送响应");
        ctx.writeAndFlush(responseProtocolMessage);
    }
}
