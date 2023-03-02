package com.newcode.community.controller;

import com.newcode.community.entity.Event;
import com.newcode.community.entity.User;
import com.newcode.community.event.EventProducer;
import com.newcode.community.service.LikeService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.HostHolder;
import com.newcode.community.util.redisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 16:11
 * @description TODO
 **/
@Controller
public class LikeController implements CommunityConstant {
    @Resource
    private LikeService likeService;
    @Resource
    private HostHolder holder;
    @Resource
    private EventProducer producer;
    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = holder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //点赞的数量
        long count = likeService.findEntityLikeCount(entityType, entityId);
        //点赞的状态
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object>map=new HashMap<>();
        map.put("likeCount",count);
        map.put("status",entityLikeStatus);

        //触发点赞事件
        if(entityLikeStatus==1){
            Event event=new Event();
            event.setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    //data里面存储点赞所在的帖子的id(点赞的不一定是帖子,也可能是回复)
                    .setData("postId",postId);
            producer.fireEvent(event);
        }
        if(entityType==ENTITY_TYPE_POST){
            //计算帖子分数
            String postScoreKey = redisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,postId);
        }

        return CommunityUtil.getJsonString(0,null,map);
    }
}
