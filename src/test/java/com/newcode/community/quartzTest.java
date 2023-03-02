package com.newcode.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/28 19:44
 * @description TODO
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NewCodeApplication.class)
public class quartzTest {
    @Resource
    private Scheduler scheduler;
    @Test
    public void test(){
        try {
            boolean res = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}