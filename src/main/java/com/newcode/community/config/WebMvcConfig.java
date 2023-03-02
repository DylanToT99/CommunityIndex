package com.newcode.community.config;

import com.newcode.community.interceptor.DataInterceptor;
import com.newcode.community.interceptor.LoginRequiredInterceptor;
import com.newcode.community.interceptor.LoginTicketInterceptor;
import com.newcode.community.interceptor.MessageInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 16:05
 * @description TODO
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;
    //@Resource
    //private LoginRequiredInterceptor loginRequiredInterceptor;

    @Resource
    private DataInterceptor dataInterceptor;
    @Resource
    private MessageInterceptor messageInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                //放行下面的路径,其他的全部拦截
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        //registry.addInterceptor(loginRequiredInterceptor)
        //        //放行下面的路径,其他的全部拦截
        //        .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                //放行下面的路径,其他的全部拦截
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(dataInterceptor)
                //放行下面的路径,其他的全部拦截
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
