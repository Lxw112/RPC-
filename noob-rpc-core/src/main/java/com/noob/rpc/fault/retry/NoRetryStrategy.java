package com.noob.rpc.fault.retry;

import com.noob.rpc.pojo.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 不重试
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy{
    /**
     * 重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
