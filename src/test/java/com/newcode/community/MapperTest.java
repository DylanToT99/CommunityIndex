package com.newcode.community;

import com.newcode.community.dao.LoginTicketMapper;
import com.newcode.community.dao.MessageMapper;
import com.newcode.community.entity.LoginTicket;
import com.newcode.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 14:49
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class MapperTest {
    @Resource
    private LoginTicketMapper loginTicketMapper;

    @Resource
    private MessageMapper messageMapper;

    @Test
    public void test(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setTicket("aabb");
        loginTicket.setStatus(1);
        loginTicket.setUserId(1001);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
        loginTicketMapper.updateStatus("aabb",0);
        loginTicketMapper.selectByTicket("aabb");
    }

    @Test
    public void test1(){
        //List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        //for (Message message : messages) {
        //    System.out.println(message);
        //}
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectUnReadLetterCount(131, "111_131"));
    }
}
