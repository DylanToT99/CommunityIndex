package com.newcode.community.event;

import com.alibaba.fastjson.JSONObject;
import com.newcode.community.entity.Event;
import com.newcode.community.util.CommunityUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/25 14:34
 * @description TODO
 **/
@Component
public class EventProducer {
    @Resource
    private KafkaTemplate kafkaTemplate;

    //提供处理事件的方法
    public void fireEvent(Event event){
        //将事件发布到指定的主题
        //将事件实体以JSON字符串的形式发送
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
