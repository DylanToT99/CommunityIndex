package com.newcode.community.service;

import com.newcode.community.dao.LoginTicketMapper;
import com.newcode.community.dao.UserMapper;
import com.newcode.community.entity.LoginTicket;
import com.newcode.community.entity.User;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.MailClient;
import com.newcode.community.util.redisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 19:43
 * @description TODO
 **/
@Service
public class UserService implements CommunityConstant {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

    //@Resource
    //private LoginTicketMapper loginTicketMapper;

    //从配置文件中取值,形式如下
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        //return userMapper.selectById(id);
        User userFromCache = getUserFromCache(id);
        if(userFromCache==null){
            userFromCache = InitCache(id);
        }
        return userFromCache;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     *
     * @param user
     * @return
     * 注册业务
     */
    public Map<String,Object>register(User user){
        Map<String,Object>map=new HashMap<>();
        //对空值进行判断处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMessage","邮箱不能为空");
            return map;
        }

        //验证账号是否已经存在
        User user1 = userMapper.selectByName(user.getUsername());
        if(user1!=null){
            map.put("usernameMessage","该账号已经存在");
            return map;
        }

        //验证邮箱
        User user2 = userMapper.selectByEmail(user.getEmail());
        if(user2!=null){
            map.put("emailMessage","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        //生成激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        //生成随机的头像地址
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context=new Context();
        //邮件的地址由context传给页面
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        //返回的url
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活牛客账号",content);

        return map;
    }

    //激活状态码
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user==null){
            return ACTIVATION_FAILURE;
        }
        if(user.getStatus()==1){
            //已经激活过了
            return ACTIVATION_REPEAT;
        }
        if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    //登录
    public Map<String,Object>login(String username,String password,int expiredSeconds){
        Map<String,Object>map=new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证是否激活
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //验证密码
        if(!CommunityUtil.md5(password + user.getSalt()).equals(user.getPassword())){
            map.put("passwordMsg","输入的密码不正确,请再次输入");
            return map;
        }

        //全部通过之后,成功登录,生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        //过期时间  勾选记住我的
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds* 1000L));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = redisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = redisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
        return (LoginTicket) redisTemplate.opsForValue().get(redisKeyUtil.getTicketKey(ticket));
    }

    public int updateHeaderImg(int userId,String url){
        int col = userMapper.updateHeaderUrl(userId, url);
        clearCache(userId);
        return col;
    }

    public int updatePassword(int userId,String newPassword){
        int col = userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return col;
    }

    //优先从缓存中取值
    private User getUserFromCache(int userId){
        String userKey = redisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }
    //取不到初始化缓存数据
    private User InitCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = redisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,60*60*2, TimeUnit.SECONDS);
        return user;
    }
    //数据变化时清除缓存数据
    private void clearCache(int userId){
        String userKey = redisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    //获得用户权限
    public Collection<? extends GrantedAuthority>   getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority>list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
