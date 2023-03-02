package com.newcode.community.controller;

import com.newcode.community.entity.DiscussPost;
import com.newcode.community.entity.Page;
import com.newcode.community.entity.User;
import com.newcode.community.service.DiscussPostService;
import com.newcode.community.service.LikeService;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 19:48
 * @description TODO
 **/
@Controller
public class HomeController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private HostHolder holder;

    @Resource
    private LikeService likeService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name = "orderMode",defaultValue = "0") int orderMode){
        //方法调用之前,springmvc会自动实例化model和page
        //而且它还会自动将page注入给model,所以在thymeleaf模板中可以直接访问page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        //找到的userid不需要,我们需要的是用户名
        List<Map<String,Object>>discussPosts=new ArrayList<>();
        if(list!=null&&!list.isEmpty()){
            for (DiscussPost post : list) {
                Map<String,Object>map=new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                int likeStatus=holder.getUser()==null?0:likeService.findEntityLikeStatus(holder.getUser().getId(),1,post.getId());
                map.put("likeCount",likeCount);
                map.put("likeStatus",likeStatus);
                discussPosts.add(map);
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("orderMode",orderMode);
        }
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }


}
