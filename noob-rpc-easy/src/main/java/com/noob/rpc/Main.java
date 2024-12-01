package com.noob.rpc;

import com.noob.rpc.server.NettyHttpServer;

public class Main {
    public static void main(String[] args) {
        NettyHttpServer httpServer = new NettyHttpServer();
        httpServer.doStart(8080);
    }
}
