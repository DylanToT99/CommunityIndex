package com.newcode.community.dao;

import com.newcode.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 19:42
 * @description TODO
 **/
@Mapper
public interface CommentMapper {

    List<Comment>selectCommentByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId,@Param("offset") int offset,@Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
