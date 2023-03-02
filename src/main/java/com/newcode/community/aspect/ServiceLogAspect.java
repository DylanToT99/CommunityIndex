package com.newcode.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 14:32
 * @description TODO
 **/
//@Component
//@Aspect
public class ServiceLogAspect {
    private static final Logger logger= LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.newcode.community.service.*.*(..))")
    public void pointcut(){
    }

    @Before("pointcut()")
    public void before(JoinPoint point){
        //用户(ip)+在[时间],访问了[com.newcode.community.service.xxx()]
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //
        if(requestAttributes==null){
            //特殊的调用
            return;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        //获取ip
        String ip = request.getRemoteHost();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String target = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();

        logger.info(String.format("用户[%s]在[%s]访问了[%s].",ip,date,target));

    }
}
