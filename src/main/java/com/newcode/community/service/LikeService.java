package com.newcode.community.service;

import com.newcode.community.util.redisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 16:03
 * @description TODO
 **/
@Service
public class LikeService {
    @Resource
    private RedisTemplate redisTemplate;

    /**
     *点赞法则:
     * 帖子的赞的entityType为1
     * 回复或者楼中楼的entityType为2
     */

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //实体获得的赞
                String entityLikeKey= redisKeyUtil.getEntityLike(entityType,entityId);
                //用户总共获得的赞
                String userLikeKey = redisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                operations.multi();
                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                        //执行事务
                return operations.exec();
            }
        });
    }
    //某个用户总共获得的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = redisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count;
    }

    //帖子或者回复的点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey= redisKeyUtil.getEntityLike(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体有没有点过赞
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey= redisKeyUtil.getEntityLike(entityType,entityId);
        //1已经点赞,0未点赞
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }



}
