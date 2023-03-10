package com.newcode.community.event;

import com.alibaba.fastjson.JSONObject;
import com.newcode.community.entity.DiscussPost;
import com.newcode.community.entity.Event;
import com.newcode.community.entity.Message;
import com.newcode.community.service.DiscussPostService;
import com.newcode.community.service.ElasticSearchService;
import com.newcode.community.service.MessageService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/25 14:36
 * @description TODO
 **/
@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Value("${wk.image.command}")
    private String command;
    @Value("${wk.image.storage}")
    private String storage;

    @Resource
    private MessageService messageService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private ElasticSearchService elasticSearchService;
    @Value("${qiniu.key.acess}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Resource
    private ThreadPoolTaskScheduler taskScheduler;

    //???message??????????????????
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("?????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("??????????????????");
        }
        //??????????????????
        Message message = new Message();
        //????????????
        message.setFromId(SYSTEM_USER_ID);
        message.setCreateTime(new Date());
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        //???????????????
        message.setStatus(0);
        //
        Map<String,Object>map=new HashMap<>();
        //map?????????????????????userID
        map.put("userId",event.getUserId());
        //?????????????????????type
        map.put("entityType",event.getEntityType());
        //?????????????????????id
        map.put("evtityId",event.getEntityId());
        if(event.getData()!=null){
            for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
                map.put(stringObjectEntry.getKey(),stringObjectEntry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));
        messageService.addMessage(message);
    }
    //??????????????????
    @KafkaListener(topics = {TOPIC_PUBLISH_POST})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("?????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("??????????????????");
        }
        DiscussPost post = discussPostService.selectDiscussPostById(event.getEntityId());
        //??????es???
        elasticSearchService.saveDiscussPost(post);
    }
    //??????????????????
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("?????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("??????????????????");
        }
       elasticSearchService.deleteDiscussPost(event.getEntityId());
    }

    //??????????????????
    @KafkaListener(topics = {TOPIC_Share})
    public void handleShare(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("?????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("??????????????????");
        }
        String htmlUrl =(String) event.getData().get("htmlUrl");
        String filename=(String) event.getData().get("filename");
        String suffix=(String) event.getData().get("suffix");
        //???????????????
        String cmd=command+" --quality 75 "+htmlUrl+" "+storage+"/"+filename+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("????????????");
        } catch (IOException e) {
            logger.error("??????????????????: "+e.getMessage());
        }

        //???????????????,???????????????,?????????????????????,??????????????????
        uploadTask uploadTask=new uploadTask(filename,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(uploadTask, 500);
        uploadTask.setFuture(future);
    }

    class uploadTask implements Runnable{
        //????????????
        private String filename;
        //????????????
        private String suffix;
        //????????????????????????,?????????????????????
        private Future future;
        //????????????
        private long startTime;
        //????????????
        private int count;
        public void setFuture(Future future) {
            this.future = future;
        }

        public uploadTask(String filename, String suffix) {
            this.filename = filename;
            this.suffix = suffix;
            this.startTime=System.currentTimeMillis();
        }

        @Override
        public void run() {
            //??????????????????
            if(System.currentTimeMillis()-startTime>30000){
                logger.error("??????????????????,???????????? "+filename);
                future.cancel(true);
                return;
            }
            //????????????
            if(count>=6){
                logger.error("??????????????????,????????????:" +filename);
                future.cancel(true);
                return;
            }
            String path=storage+"/"+filename+suffix;
            File file=new File(path);
            if(file.exists()){
                logger.info(String.format("?????????%d?????????[%s]",++count,filename));
                //??????????????????
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));
                //??????????????????
                Auth auth=Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, filename, 3600, policy);
                //?????????????????????
                UploadManager uploadManager=new UploadManager(new Configuration(Region.huadong()));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    //??????????????????
                    Response response=uploadManager.put(
                            path,filename,uploadToken,null,"image"+suffix,false
                    );
                    //??????????????????
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    System.out.println(jsonObject.get("code"));
                    if(jsonObject==null||jsonObject.get("code")==null||!jsonObject.get("code").toString().equals("0")){
                        logger.info(String.format("???%d????????????[%s]",count,filename));
                    }else{
                        logger.info(String.format("???%d????????????[%s]",count,filename));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info(String.format("???%d????????????[%s]",count,filename));
                }
            }else{
                logger.info("?????????????????? ["+filename+"]");
            }
        }
    }
}
