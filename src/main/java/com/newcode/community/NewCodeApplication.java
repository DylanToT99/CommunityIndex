package com.newcode.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NewCodeApplication {
    //
    //@PostConstruct
    //public void init(){
    //    //解决netty启动冲突的问题
    //    //netty4utils
    //
    //
    //}
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(NewCodeApplication.class, args);
    }

}
