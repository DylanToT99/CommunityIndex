package com.newcode.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/25 13:41
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class kafkaTest {
    @Resource
    private kafkaProduce kafkaProduce;
    @Test
    public void testKafka() throws InterruptedException {
        kafkaProduce.sendMessage("test","howareyou");
        kafkaProduce.sendMessage("test","ok?");
    }
}
@Component
class kafkaProduce{

    @Resource
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class kafkaConsumer{
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
