package com.newcode.community.service;

import com.newcode.community.entity.User;
import com.newcode.community.util.redisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/24 14:38
 * @description TODO
 **/
@Service
public class FollowService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserService userService;

    //关注某个实体
    public void follow(int userId,int entityId,int entityType){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //被哪些用户关注的key
                String followerKey = redisKeyUtil.getFollowerKey(entityId, entityType);
                //关注了哪些实体key
                String followeeKey = redisKeyUtil.getFolloweeKey(userId, entityType);
                //redis进入事务
                operations.multi();
                //将自己的id添加到被哪些用户关注的key
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                //将该实体添加到自己的关注列表
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                //提交事务
                return operations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId,int entityId,int entityType){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //被哪些用户关注的key
                String followerKey = redisKeyUtil.getFollowerKey(entityId, entityType);
                //关注了哪些实体key
                String followeeKey = redisKeyUtil.getFolloweeKey(userId, entityType);
                operations.multi();
                //将自己的id从到被哪些用户关注的key中删除
                operations.opsForZSet().remove(followerKey,userId);
                //将该实体从到自己的关注列表删除
                operations.opsForZSet().remove(followeeKey,entityId);
                return operations.exec();
            }
        });
    }
    //查询某个用户关注实体的数量
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = redisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询用户的关注
    public List<Map<String,Object>>getFollowees(int entityType, int userId,int offset,int limit){
        String followerKey = redisKeyUtil.getFolloweeKey(userId, entityType);
        //获得所有有序集合内的元素按照分数从高到低排序
        Set<Integer> targetSet = redisTemplate.opsForZSet().reverseRange(followerKey, offset, limit + limit - 1);

        if(targetSet==null){
            return null;
        }
        List<Map<String,Object>>list=new ArrayList<>();
        for (Integer targetId : targetSet) {
            Map<String,Object>map=new HashMap<>();
            //粉丝实体
            User user = userService.findUserById(targetId);
            map.put("user",user);
            //关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某个实体的粉丝数量
    public long findFollowerCount(int entityType,int entityId){
        String followerKey = redisKeyUtil.getFollowerKey(entityId, entityType);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询某个实体粉丝的ids
    public List<Map<String,Object>>getFollowers(int entityType, int entityId,int offset,int limit){
        String followerKey = redisKeyUtil.getFollowerKey(entityId, entityType);
        //获得所有有序集合内的元素按照分数从高到低排序
        Set<Integer> fanSet = redisTemplate.opsForZSet().reverseRange(followerKey, offset, limit + limit - 1);

        if(fanSet==null){
            return null;
        }
        List<Map<String,Object>>list=new ArrayList<>();
        for (Integer fanId : fanSet) {
            Map<String,Object>map=new HashMap<>();
            //粉丝实体
            User user = userService.findUserById(fanId);
            map.put("user",user);
            //关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, fanId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询当前用户是否关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = redisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }
}
