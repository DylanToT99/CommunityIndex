package com.newcode.community.dao;

import com.newcode.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 18:21
 * @description TODO
 **/
@Mapper
@Repository
public interface DiscussPostMapper {
    //查询评论,带有分页
    List<DiscussPost>selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    //@param用于给方法取别名
    //若方法只有一个参数,并且在<if>中使用,则一定要加param
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost post);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

    int updateScore(int id,double score);
}
