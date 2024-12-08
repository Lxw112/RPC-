package com.noob.rpc.samplespringbootconsumer;


import com.noob.rpc.entity.User;
import com.noob.rpc.noobrpcspringbootstarter.annotation.RpcReference;
import com.noob.rpc.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @RpcReference
    private UserService userService;
    @RequestMapping("/hello")
    public String fet(){
        User user = new User("我的名字叫做heartunderblade");
        User resultUser = userService.getUser(user);
        System.out.printf(resultUser.getName());
        return resultUser.getName();
    }
}
