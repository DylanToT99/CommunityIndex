package com.newcode.community.service;

import com.newcode.community.dao.MessageMapper;
import com.newcode.community.entity.Message;
import com.newcode.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/22 19:30
 * @description TODO
 **/
@Service
public class MessageService {
    @Resource
    private MessageMapper mapper;
    @Resource
    private SensitiveFilter filter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return mapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return mapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return mapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCount(String conversationId){
        return mapper.selectLetterCount(conversationId);
    }

    public int findUnreadLetter(int userId,String conversationId){
        return mapper.selectUnReadLetterCount(userId,conversationId);
    }

    /**
     * 发送私信
     */
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(filter.filter(message.getContent()));
        return mapper.insertMessage(message);
    }

    /**
     * 改变消息状态
     */
    public int readMessage(List<Integer>ids){
        return mapper.updateStatus(ids,1);
    }

    /**
     *查询最新通知
     */
    public Message findLatestNotice(int userId,String topic){
        return mapper.selectLatestNotice(userId,topic);
    }
    /**
     * 查询消息数量
     */

    public int findNoticeCount(int userId,String topic){
        return mapper.selectNoticeCount(userId,topic);
    }

    public  int findNoticeUnreadCount(int userId,String topic){
        return mapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message>findNotices(int userId,String topic,int offset,int limit){
        return mapper.selectNotice(userId,topic,offset,limit);
    }
}
