package com.noob.rpc.handler;

import com.noob.rpc.pojo.RpcResponse;
import com.noob.rpc.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TcpResponseServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<?>> {
    public static final Map<Long, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<?> msg) throws Exception {
        log.debug("{}",msg);
        //拿到空的promise
        Promise<Object> promise = PROMISES.remove(msg.getHeader().getRequestId());
        if (promise != null){
            RpcResponse response = (RpcResponse) msg.getBody();
            Exception exceptionValue = response.getException();
            if (exceptionValue != null){
                promise.setFailure(exceptionValue);
            }else {
                promise.setSuccess(response);
            }
        }
    }
}
