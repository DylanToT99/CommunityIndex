package com.newcode.community.quartz;

import com.newcode.community.entity.DiscussPost;
import com.newcode.community.service.DiscussPostService;
import com.newcode.community.service.ElasticSearchService;
import com.newcode.community.service.LikeService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.redisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/28 20:06
 * @description TODO
 **/
public class PostScoreRefreshJob implements Job, CommunityConstant {
    private Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;
    @Resource
    private ElasticSearchService elasticSearchService;
    //牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败");
        }
    }
    //统计分数
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = redisKeyUtil.getPostScoreKey();
        BoundSetOperations Operations=redisTemplate.boundSetOps(postScoreKey);
        //没有任何变化
        if(Operations.size()==0){
            logger.info("任务取消,没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始],正在刷新帖子分数:"+Operations.size());
        while (Operations.size()>0){
            //刷新帖子的分数
            this.refresh((Integer)Operations.pop());
        }
        logger.info("[任务结束]帖子分数刷新完毕");
    }
    private void refresh(int postId){
        DiscussPost post = discussPostService.selectDiscussPostById(postId);
        if(post==null){
            logger.error("该帖子不存在: id="+postId);
            return;
        }
        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double w=(wonderful?75:0)+commentCount*10+likeCount*2;
        //分数=帖子的权重加上距离天数
        double score=Math.log10(Math.max(w,1))
                +(post.getCreateTime().getTime()-epoch.getTime())/(1000*60*60*24);
        //更新帖子分数
        discussPostService.updateScore(postId,score);
        //同步es
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
