package com.newcode.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/18 13:53
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class LoggerTest {
    private static final Logger logger=LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger(){
        System.out.println(logger.getName());

        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");

    }
}
