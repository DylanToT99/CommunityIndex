package com.newcode.community;

import com.newcode.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 12:07
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class sensitiveWordsTest {
    @Resource
    SensitiveFilter sensitiveFilter;

    @Test
    public void test(){
        String text="我草你的";
        System.out.println(sensitiveFilter.filter(text));
    }
}
