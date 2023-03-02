package com.newcode.community.controller;

import com.newcode.community.entity.Comment;
import com.newcode.community.entity.DiscussPost;
import com.newcode.community.entity.Event;
import com.newcode.community.event.EventProducer;
import com.newcode.community.service.CommentService;
import com.newcode.community.service.DiscussPostService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.HostHolder;
import com.newcode.community.util.redisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/22 15:10
 * @description TODO
 **/
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Resource
    private CommentService commentService;
    @Resource
    private HostHolder holder;

    @Resource
    private EventProducer producer;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){

        if(comment==null||comment.getContent()==null||"".equals(comment.getContent())){
            return "redirect:/discuss/detail/"+discussPostId;
        }
         comment.setUserId(holder.getUser().getId());
         comment.setStatus(0);
         comment.setCreateTime(new Date());
         commentService.addComment(comment);

         //触发评论事件--推送
        Event event=new Event();
        event.setTopic(TOPIC_COMMENT).setUserId(holder.getUser().getId()).setEntityId(comment.getId())
                .setEntityType(comment.getEntityType()).setData("postId",discussPostId);

        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        }else if(ENTITY_TYPE_COMMENT==comment.getEntityType()){
            Comment comment1 = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(comment1.getUserId());
        }
         producer.fireEvent(event);

        //回复帖子相当于修改了帖子的状态,需要修改信息到es
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //触发评论事件-->搜索
            event=new Event().setTopic(TOPIC_PUBLISH_POST)
                    .setUserId(comment.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            //计算帖子分数
            String postScoreKey = redisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,discussPostId);
        }
        return "redirect:/discuss/detail/"+discussPostId;
    }

}
