package com.newcode.community.util;

import org.springframework.http.HttpRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 16:12
 * @description TODO
 **/
public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name){
        if(name==null||request==null){
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
