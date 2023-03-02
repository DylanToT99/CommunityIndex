package com.newcode.community.interceptor;

import com.newcode.community.entity.LoginTicket;
import com.newcode.community.entity.User;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CookieUtil;
import com.newcode.community.util.HostHolder;
import com.newcode.community.util.redisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 16:11
 * @description TODO
 **/
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if(ticket!=null){
            //表示登录了
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                hostHolder.setUser(user);
                //构建用户认证的结果,并存入securityContext,便于security授权
                Authentication authentication=new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                //增加登录凭证的有效时间
                Date now = new Date();
                //相差的毫秒数
                long diff=loginTicket.getExpired().getTime()-now.getTime();
                if(diff/(1000*60*60)<2){
                    //凭证加两个小时
                    long newTime=now.getTime()+2*60*60*1000;
                    Date newExpired = new Date(newTime);
                    loginTicket.setExpired(newExpired);
                    String ticketKey = redisKeyUtil.getTicketKey(loginTicket.getTicket());
                    redisTemplate.opsForValue().set(ticketKey,ticket);
                }

            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
