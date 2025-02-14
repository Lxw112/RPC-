package com.noob.rpc.samplespringbootconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SampleSpringbootConsumerApplicationTests {

    @Resource
    private UserServiceImpl userService;
    @Test
    void contextLoads() {
        userService.getName();
    }

    @Test
    void test1(){
        userService.getName();
    }
}
