package com.newcode.community;

import com.newcode.community.dao.DiscussPostMapper;
import com.newcode.community.dao.UserMapper;
import com.newcode.community.entity.DiscussPost;
import com.newcode.community.entity.User;
import com.newcode.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
class NewCodeApplicationTests implements ApplicationContextAware {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

   @Test
    public void testApplication(){
        System.out.println(applicationContext);
    }

    @Test
    public void testSelectUser(){
        User user= userMapper.selectById(1);
        System.out.println(user);
        user=userMapper.selectByEmail("nowcoder22@sina.com");
        System.out.println(user);
        user=userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testSelectPost(){
        int rows = discussPostMapper.selectDiscussPostRows(101);
        System.out.println(rows);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(101, 0, 5,0);
        discussPosts.forEach(System.out::println);
    }


    @Test
    public void testPassword(){
        String s = CommunityUtil.md5("123456" + "496eb");
        System.out.println(s);
    }
}
