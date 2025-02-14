package com.noob.rpc.samplespringbootconsumer;

import com.noob.rpc.noobrpcspringbootstarter.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableRpc(needServer = false)
@SpringBootApplication
public class SampleSpringbootConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleSpringbootConsumerApplication.class, args);
    }

}
