package com.newcode.community.controller;

import com.newcode.community.entity.Event;
import com.newcode.community.entity.Page;
import com.newcode.community.entity.User;
import com.newcode.community.event.EventProducer;
import com.newcode.community.service.FollowService;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/24 14:42
 * @description TODO
 **/
@Controller
public class FollowController implements CommunityConstant {
    @Resource
    private FollowService followService;
    @Resource
    private HostHolder holder;
    @Resource
    private UserService userService;
    @Resource
    private EventProducer producer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityId,int entityType){
        User user = holder.getUser();
        if(user.getId()==entityId&&entityType==ENTITY_TYPE_USER){
            return CommunityUtil.getJsonString(1,"用户不能关注自己");
        }
        followService.follow(user.getId(),entityId,entityType);
        //触发关注时间
        Event event=new Event();
        event.setUserId(user.getId())
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                //注意此处只针对用户,如果后续拓展关注的帖子的功能,此处需要修改
                .setEntityUserId(entityId);
        producer.fireEvent(event);
        return CommunityUtil.getJsonString(0,"已关注");
    }
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityId,int entityType){
        User user = holder.getUser();
        followService.unfollow(user.getId(),entityId,entityType);
        return CommunityUtil.getJsonString(0,"已取消关注");
    }
    @GetMapping("/follower/{userId}")
    public String follower(Model model, Page page, @PathVariable("userId")int userId){
        //查询用户
        User user = userService.findUserById(userId);
        if(user==null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/follower/"+user.getId());
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        List<Map<String,Object>> userList=followService.getFollowers(ENTITY_TYPE_USER,user.getId(),page.getOffset(),page.getLimit());
        if (userList!=null){
            for (Map<String, Object> map : userList) {
                //关注的人
                User u = (User)map.get("user");
                boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    @GetMapping("/followee/{userId}")
    public String followee(Model model, Page page, @PathVariable("userId")int userId){
        //查询用户
        User user = userService.findUserById(userId);
        if(user==null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followee/"+user.getId());
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        List<Map<String,Object>> userList=followService.getFollowees(ENTITY_TYPE_USER,user.getId(),page.getOffset(),page.getLimit());
        if (userList!=null){
            for (Map<String, Object> map : userList) {
                //关注的人的关注的人
                User u = (User)map.get("user");
                boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }
    //当前用户是否关注传入的用户
    private boolean hasFollowed(int userId){
        if(holder.getUser()==null){
            return false;
        }
        return followService.hasFollowed(holder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}
