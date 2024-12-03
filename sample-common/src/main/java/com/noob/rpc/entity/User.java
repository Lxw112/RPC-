package com.noob.rpc.entity;

import java.io.Serializable;

/**
 * 用户信息
 */
public class User implements Serializable {
    private String name;

    public User(String name){
        this.name = name;
    }

    public User(){

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
