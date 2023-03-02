package com.newcode.community.dao;

import com.newcode.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/22 18:36
 * @description TODO
 **/
@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表,针对每个会话只返回最新数据
    List<Message>selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message>selectLetters(String conversationId,int offset,int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectUnReadLetterCount(int userId,String conversationId);

    //增加消息
    int insertMessage(Message message);

    //修改消息的状态
    int updateStatus(List<Integer>ids,int status);

    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId,String topic);
    //查询某个主题的通知数量
    int selectNoticeCount(int userId,String topic);
    //查询某个主题的未读数量
    int selectNoticeUnreadCount(int userId,String topic);

    //查询某个主题所包含的通知列表
    List<Message>selectNotice(int userId,String topic,int offset,int limit);
}
