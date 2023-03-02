package com.newcode.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.newcode.community.entity.Message;
import com.newcode.community.entity.Page;
import com.newcode.community.entity.User;
import com.newcode.community.service.MessageService;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/22 19:35
 * @description TODO
 **/
@Controller
public class MessageController implements CommunityConstant {
    //处理私信列表请求
    @Resource
    private MessageService messageService;
    @Resource
    private HostHolder holder;

    @Resource
    private UserService userService;

    //用户的私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        //配置分页信息
        User user = holder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>>conversationList=new ArrayList<>();
        if (conversations!=null) {
            for (Message conversation : conversations) {
                Map<String,Object>map=new HashMap<>();
                map.put("conversation",conversation);
                //用户未读消息数量
                map.put("unreadCount",messageService.findUnreadLetter(user.getId(), conversation.getConversationId()));
                //某一条会话的消息数量
                map.put("letterCount",messageService.findLetterCount(conversation.getConversationId()));
                int targetId=user.getId()==conversation.getFromId()?conversation.getToId():conversation.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversationList.add(map);
            }
        }
        model.addAttribute("conversations",conversationList);

        //总共的未读数量
        int unreadLetterTotal = messageService.findUnreadLetter(user.getId(), null);
        model.addAttribute("unreadLetterTotal",unreadLetterTotal);
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }

    //用户某一条私信的具体详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //私信具体列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>>letters=new ArrayList<>();
        if(letterList!=null){
            for (Message message : letterList) {
                Map<String,Object>map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",targetUser(conversationId));

        //设置已读
        List<Integer>ids=getLetterIds(letterList);
        if(!ids.isEmpty()){
             messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    //得到集合中未读的消息的id
    private List<Integer> getLetterIds(List<Message>letterList){
        List<Integer>ids=new ArrayList<>();
        if (letterList!=null) {
            for (Message message : letterList) {
                //当前用户是否是接受者
                if(holder.getUser().getId()==message.getToId()&&message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    //分解conversationId
    private User targetUser(String conversationId){
        String[] strings = conversationId.split("_");
        int id1=Integer.parseInt(strings[0]);
        int id2=Integer.parseInt(strings[1]);
        if(holder.getUser().getId()==id1){
            return userService.findUserById(id2);
        }else{
            return userService.findUserById(id1);
        }
    }

    //异步请求
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target==null){
            return CommunityUtil.getJsonString(1,"目标用户不存在");
        }
        Message message=new Message();
        message.setFromId(holder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        if (message.getFromId()<message.getToId()) {
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0);
    }

    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = holder.getUser();

        //查询评论内容
        Message message=messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object>messageVo=new HashMap<>();
        messageVo.put("message",message);
        if(message!=null){
            String content= HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((int)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count=messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);

            int unreadCount=messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread",unreadCount);
            model.addAttribute("commentNotice",messageVo);
        }
        //查询点赞内容
        message=messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
        messageVo=new HashMap<>();
        messageVo.put("message",message);
        if(message!=null){

            String content= HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((int)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count=messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);

            int unreadCount=messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread",unreadCount);
            model.addAttribute("likeNotice",messageVo);
        }
        //查询关注内容
        message=messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        messageVo=new HashMap<>();
        messageVo.put("message",message);
        if(message!=null){

            String content= HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((int)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count=messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);

            int unreadCount=messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread",unreadCount);
            model.addAttribute("followNotice",messageVo);
        }
        //查询未读消息数量
        int unreadLetterCount = messageService.findUnreadLetter(user.getId(), null);
        model.addAttribute("unreadLetterCount",unreadLetterCount);
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    public String noticeDetail(@PathVariable("topic")String topic,Model model,Page page){
        User user = holder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>>noticeVoList=new ArrayList<>();
        if (notices!=null){
            for (Message notice : notices) {
                Map<String,Object>map=new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((int)data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知的作者
                User fromUser = userService.findUserById(notice.getFromId());
                map.put("fromUser",fromUser);

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);
        //设置已读
        List<Integer>ids=getLetterIds(notices);
        if(ids!=null&& !ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
