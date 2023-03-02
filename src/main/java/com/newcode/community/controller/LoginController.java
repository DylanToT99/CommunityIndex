package com.newcode.community.controller;

import com.google.code.kaptcha.Producer;
import com.newcode.community.entity.User;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.redisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/18 15:01
 * @description TODO
 **/
@Controller
public class LoginController implements CommunityConstant {

    private static final org.slf4j.Logger logger=LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;
    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }


    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //把验证码存入session
        //session.setAttribute("kaptcha",text);

        //验证码的归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        //验证码有效时间:120s
        cookie.setMaxAge(120);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey = redisKeyUtil.getKaptchaKey(kaptchaOwner);
        //有效时间120s
        redisTemplate.opsForValue().set(redisKey,text,120, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
           logger.error("响应验证码失败:"+e.getMessage());
        }
    }

    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMessage",map.get("usernameMessage"));
            model.addAttribute("passwordMessage",map.get("passwordMessage"));
            model.addAttribute("emailMessage",map.get("emailMessage"));
            return "/site/register";
        }
    }
    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code")String code){
        int activation = userService.activation(userId, code);
        if(activation==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功!您的账号已经可以正常使用");
            model.addAttribute("target","/login");
        } else if(activation==ACTIVATION_REPEAT){
            model.addAttribute("msg","该账号已经激活,请勿重复激活");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败,您的激活码不正确");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    /**
     * 处理登录请求
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){
        //检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptchaOwner)){
            model.addAttribute("codeMsg","验证码已经过期,请重新输入");
            return "/site/login";
        }
        String redisKey = redisKeyUtil.getKaptchaKey(kaptchaOwner);
        String kaptchaCode =(String) redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(code)||StringUtils.isBlank(kaptchaCode)||!kaptchaCode.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds=rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //如果map中包含"ticket",则说明用户的账号密码信息正确,可以登录
        if(map.containsKey("ticket")){
            //拿到cookie信息
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            //设置cookie有效的路径: 整个项目
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            //将设置好的cookie返回给浏览器
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            //用户在登录的某一环节出了差错
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        //默认get请求
        return "redirect:/login";
    }
}
