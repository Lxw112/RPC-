package com.noob.rpc.samplespringbootprovider;

import com.noob.rpc.noobrpcspringbootstarter.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableRpc
@SpringBootApplication
public class SampleSpringbootProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleSpringbootProviderApplication.class, args);
    }

}
