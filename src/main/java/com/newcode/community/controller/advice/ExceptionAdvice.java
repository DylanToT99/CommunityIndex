package com.newcode.community.controller.advice;

import com.newcode.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 13:57
 * @description TODO
 **/

//扫描带有controller的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);


    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常:"+e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            logger.error(stackTraceElement.toString());
        }
        //获取请求方式
        String xRequestedWith = request.getHeader("x-requested-with");
        //该请求是异步请求,返回json
        if ("XMLHttpRequest".equals(xRequestedWith)){
            //设置响应格式为普通文本
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJsonString(1,"服务器异常"));
        }else {
            //普通请求
            //普通请求直接重定向到error界面
            response.sendRedirect(request.getContextPath()+"/error");
        }

    }

}
