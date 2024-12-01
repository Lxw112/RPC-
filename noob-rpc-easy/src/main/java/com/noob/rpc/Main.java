package com.noob.rpc;

public class Main {
    public static void main(String[] args) {
        NettyHttpServer httpServer = new NettyHttpServer();
        httpServer.doStart(8080);
    }
}
