package com.newcode.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.newcode.community.dao.DiscussPostMapper;
import com.newcode.community.entity.DiscussPost;
import com.newcode.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 19:40
 * @description TODO
 **/
@Service
public class DiscussPostService {
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;

    private static final Logger logger= LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //caffeine核心接口： cache,loadingCache,AsyncLoadingCache

    //帖子列表的缓存
    private LoadingCache<String,List<DiscussPost>>postListCache;

    //缓存帖子的总数
    private LoadingCache<Integer,Integer>rowsCache;

    @PostConstruct
    public void init(){
        //初始化列表缓存
        postListCache= Caffeine.newBuilder().maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key.length()==0){
                            throw new IllegalArgumentException("参数错误");
                        }
                        String[] params = key.split(":");
                        if(params.length!=2){
                            throw new IllegalArgumentException("参数错误");
                        }
                        int offset=Integer.parseInt(params[0]);
                        int limit=Integer.parseInt(params[1]);
                        //也可以添加redis缓存作为二级缓存
                        //最后访问数据库
                        logger.info("load post from db");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化帖子总数缓存
        rowsCache=Caffeine.newBuilder().maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer integer) throws Exception {
                        logger.info("load rows from db");
                        return discussPostMapper.selectDiscussPostRows(integer);
                    }
                });
    }


    public List<DiscussPost>findDiscussPosts(int userId,int offset,int limit,int orderMode){
        if(userId==0&&orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.info("load post from db");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId==0){
            return rowsCache.get(userId);
        }
        logger.info("load rows from db");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //处理标签
        //转义HTML标签(比如在标题或内容中写<h1>你好<h1>,会将大于号小于号视为普通字符而不是标签)
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        //插入
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost selectDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    //添加评论数量
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }
}
