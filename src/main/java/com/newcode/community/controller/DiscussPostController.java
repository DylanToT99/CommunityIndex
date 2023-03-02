package com.newcode.community.controller;

import com.newcode.community.entity.*;
import com.newcode.community.event.EventProducer;
import com.newcode.community.service.*;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.HostHolder;
import com.newcode.community.util.redisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 15:40
 * @description TODO
 **/
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private HostHolder holder;
    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private LikeService likeService;
    //@Resource
    //private ElasticSearchService elasticSearchService;
    @Resource
    private EventProducer eventProducer;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = holder.getUser();
        if(user==null){
            return CommunityUtil.getJsonString(403,"您还未登录,请登录后再进行发帖");
        }
        if(StringUtils.isBlank(title)||StringUtils.isBlank(content)){
            return CommunityUtil.getJsonString(1,"标题或内容不能为空");
        }
        DiscussPost post=new DiscussPost();
        post.setTitle(title);
        post.setUserId(user.getId());
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        //触发发帖事件
        Event event=new Event().setTopic(TOPIC_PUBLISH_POST)
                .setUserId(user.getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //计算帖子分数
        String postScoreKey = redisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,post.getId());
        //报错将来统一处理
        return CommunityUtil.getJsonString(0,"发布成功!");
    }

    /**
     *理一下大概逻辑:
     * 首先每个帖子有记录评论帖子的数量
     * 而回复帖子的评论又有直接回复楼主的回复和回复楼里其他人的回复
     * comment字段中包含该条comment的作者的userId,以及该条comment的类型entityType(1代表回复帖子的评论,2代表回复帖子里的评论的评论)
     *entityId代表回复得对象的id,如当entityType为1时,那么entityId就是回复得主题帖子的id.
     * 如果是2,那么在楼中的所有回复的entityId都是该楼主发的评论的id
     * 而如果targetId不为0的话,说明该条回复是回复别人楼中楼的,那么targetId就是回复对象的userId
     */

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId")int id, Model model, Page page){
        //帖子
        DiscussPost post = discussPostService.selectDiscussPostById(id);
        model.addAttribute("post",post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount",likeCount);
        int likeStatus = holder.getUser()==null?0:
                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatus",likeStatus);
        //评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+id);
        //单个帖子的评论总数
        page.setRows(post.getCommentCount());
        //给帖子的评论:评论
        //给评论的评论:回复
        List<Comment> commentList =
                commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论的列表
        List<Map<String,Object>>commentVoList=new ArrayList<>();
        if(commentList!=null&&!commentList.isEmpty()){
            for (Comment comment : commentList) {
                //一个评论的vo
                Map<String,Object>commentVo=new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //评论的作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //查询回复列表
                List<Comment> replyList =
                        commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                //赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                likeStatus = holder.getUser()==null?0:
                        likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复VO列表
                List<Map<String,Object>>replyVoList=new ArrayList<>();
                if(replyList!=null){
                    for (Comment reply : replyList) {
                        Map<String,Object>replyVo=new HashMap<>();
                        //存回复:
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复的目标
                        if(reply.getTargetId()!=0){
                            User target = userService.findUserById(reply.getTargetId());
                            replyVo.put("target",target);
                        }else{
                            replyVo.put("target",null);
                        }
                        //点赞状态
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        likeStatus = holder.getUser()==null?0:
                                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复的数量
                int commentCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",commentCount);
                commentVoList.add(commentVo);
            }
            model.addAttribute("comments",commentVoList);
        }
        return "/site/discuss-detail";
    }
    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        //触发发帖事件
        Event event=new Event().setTopic(TOPIC_PUBLISH_POST)
                .setUserId(holder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //报错将来统一处理
        return CommunityUtil.getJsonString(0);
    }
    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        //触发发帖事件
        Event event=new Event().setTopic(TOPIC_PUBLISH_POST)
                .setUserId(holder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String postScoreKey = redisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,id);
        //报错将来统一处理
        return CommunityUtil.getJsonString(0);
    }
    //删除
    @PostMapping("/delete")
    @ResponseBody
    public String delete(int id){
        discussPostService.updateStatus(id,2);
        //触发删帖事件
        Event event=new Event().setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //报错将来统一处理
        return CommunityUtil.getJsonString(0);
    }

}
