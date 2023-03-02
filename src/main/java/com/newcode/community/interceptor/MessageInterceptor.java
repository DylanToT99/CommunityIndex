package com.newcode.community.interceptor;

import com.newcode.community.entity.User;
import com.newcode.community.service.MessageService;
import com.newcode.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/25 19:05
 * @description TODO
 **/
@Component
public class MessageInterceptor implements HandlerInterceptor{
    @Resource
    private HostHolder holder;

    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = holder.getUser();
        if(user!=null&&modelAndView!=null){
            int unreadLetter = messageService.findUnreadLetter(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount",noticeUnreadCount+unreadLetter);
        }
    }
}
