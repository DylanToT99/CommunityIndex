package com.newcode.community;

import com.newcode.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/18 14:40
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class MailTest {
        @Resource
        private MailClient mailClient;

        @Resource
        private TemplateEngine templateEngine;

        @Test
        public void testHtml(){
                Context context=new Context();
                String username="Dylan";
                context.setVariable("username",username);
                String content = templateEngine.process("/mail/demo", context);
                System.out.println(content);
                mailClient.sendMail("1105312412@qq.com","HTML",content);
        }

        @Test
        public void testMail(){
                mailClient.sendMail("1105312412@qq.com","TEST","Test MailSender");
        }
}
