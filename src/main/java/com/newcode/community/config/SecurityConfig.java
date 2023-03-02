package com.newcode.community.config;

import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/27 16:24
 * @description TODO
 **/
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant  {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //@Override
    //protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //    super.configure(auth);
    //}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                //需要登录才能访问的路径
                .antMatchers(
                    "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                //这三个任意一个都可以
                .hasAnyAuthority(
                    AUTHORITY_ADMIN,AUTHORITY_USER,AUTHORITY_MODERATOR
                )
                .antMatchers(
                    "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_MODERATOR,AUTHORITY_ADMIN
                ) .antMatchers(
                        "/discuss/delete",
                "/data/**","/actuator/**"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                //除了上面的几个请求,其他的谁都可以访问
                .anyRequest().permitAll().and()
                //禁用csrf检查，正式环境需要开开
                .csrf().disable();

        //权限不够时怎么处理
        http.exceptionHandling()
                //没有登录时怎么处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        //判断当前请求是普通请求还是异步请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //是异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"您还未登录"));
                        }else{
                            //是普通请求
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                //权限不足时怎么处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        //判断当前请求是普通请求还是异步请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //是异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"您没有访问此功能的权限"));
                        }else{
                            //是普通请求
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });

        //Security默认会拦截/logout的请求,进行退出处理
        //覆盖它默认的逻辑,才能执行我们自己退出的代码
        http.logout()
                .logoutUrl("/security/logout");

    }
}
