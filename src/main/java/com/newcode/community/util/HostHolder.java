package com.newcode.community.util;

import com.newcode.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 16:20
 * @description 持有用户的信息,用于代替session对象
 **/
@Component
public class HostHolder {

    private ThreadLocal<User> userThreadLocal=new ThreadLocal<>();

    public void setUser(User user){
        userThreadLocal.set(user);
    }

    public User getUser(){
        return userThreadLocal.get();
    }

    public void clear(){
        userThreadLocal.remove();
    }

}
