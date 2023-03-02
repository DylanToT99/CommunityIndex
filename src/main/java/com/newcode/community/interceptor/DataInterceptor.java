package com.newcode.community.interceptor;

import com.newcode.community.controller.DataService;
import com.newcode.community.entity.User;
import com.newcode.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/28 15:47
 * @description TODO
 **/
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Resource
    private DataService dataService;
    @Resource
    private HostHolder holder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        String ip=request.getRemoteHost();
        dataService.recordUV(ip);
        //统计DAU
        User user = holder.getUser();
        if(user!=null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
