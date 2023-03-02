package com.newcode.community.controller;

import com.newcode.community.entity.DiscussPost;
import com.newcode.community.entity.Page;
import com.newcode.community.entity.SearchResult;
import com.newcode.community.service.ElasticSearchService;
import com.newcode.community.service.LikeService;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/26 20:18
 * @description TODO
 **/
@Controller
public class SearchController implements CommunityConstant {
    @Resource
    private ElasticSearchService elasticSearchService;
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        SearchResult searchResult = elasticSearchService.SearchResult(keyword, page.getOffset(), page.getLimit());
        List<Map<String,Object>>discussPosts=new ArrayList<>();
        List<DiscussPost> list = searchResult.getList();
        if(list!=null){
            for (DiscussPost post : list) {
                Map<String,Object>map=new HashMap<>();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));

                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
        //分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult.getTotal()==0?0:(int)searchResult.getTotal());
        return "/site/search";
    }
}
