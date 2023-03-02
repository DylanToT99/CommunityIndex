package com.newcode.community.service;

import com.newcode.community.dao.CommentMapper;
import com.newcode.community.entity.Comment;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 19:51
 * @description TODO
 **/
@Service
public class CommentService implements CommunityConstant {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Comment>findCommentByEntity(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    /**
     * 添加帖子
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        //过滤html标签
        comment.setContent(HtmlUtils.htmlUnescape(comment.getContent()));
        //过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //更新回复给帖子的评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }
}
