package com.newcode.community.config;

import com.newcode.community.quartz.AlphaJob;
import com.newcode.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/28 19:26
 * @description 配置->数据库->访问数据库调用任务而不再访问配置
 **/
@Configuration
public class QuartzConfig {

    //FactoryBean可简化bean的实例化过程
    //spring通过factorybean封装bean的实例化过程
    //可以将factorybean装配到spring容器里
    //将factorybean注入给其他bean
    //该bean得到的是factorybean所管理的对象实例


    //配置jobDetail
    //@Bean
    //public JobDetailFactoryBean alphaJobDetail(){
    //    JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
    //    factoryBean.setJobClass(AlphaJob.class);
    //    factoryBean.setName("alphaJob");
    //    factoryBean.setGroup("alphaJobGroup");
    //    //任务是长久保存
    //    factoryBean.setDurability(true);
    //    //任务是否是可恢复的
    //    factoryBean.setRequestsRecovery(true);
    //    return factoryBean;
    //}
    //                      简单                      复杂
    //配置trigger(SimpleTriggerFactoryBean or CronTriggerFactoryBean)
    //@Bean
    //public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaDetail){
    //    SimpleTriggerFactoryBean stfb=new SimpleTriggerFactoryBean();
    //    stfb.setJobDetail(alphaDetail);
    //    stfb.setName("alphaTrigger");
    //    stfb.setGroup("alphaTriggerGroup");
    //    stfb.setRepeatInterval(3000);
    //    stfb.setJobDataMap(new JobDataMap());
    //    return stfb;
    //}

    //配置刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        //任务是长久保存
        factoryBean.setDurability(true);
        //任务是否是可恢复的
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }
    //                      简单                      复杂
    //配置trigger(SimpleTriggerFactoryBean or CronTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean stfb=new SimpleTriggerFactoryBean();
        stfb.setJobDetail(postScoreRefreshJobDetail);
        stfb.setName("postScoreRefreshTrigger");
        stfb.setGroup("communityTriggerGroup");
        //五分钟执行一遍
        stfb.setRepeatInterval(1000*60*5);
        stfb.setJobDataMap(new JobDataMap());
        return stfb;
    }
}
