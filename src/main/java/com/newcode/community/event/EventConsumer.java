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

    //往message表中插入数据
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误");
        }
        //发送站内通知
        Message message = new Message();
        //来自系统
        message.setFromId(SYSTEM_USER_ID);
        message.setCreateTime(new Date());
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        //消息为未读
        message.setStatus(0);
        //
        Map<String,Object>map=new HashMap<>();
        //map携带产生事件的userID
        map.put("userId",event.getUserId());
        //事件作用对象的type
        map.put("entityType",event.getEntityType());
        //事件作用对象的id
        map.put("evtityId",event.getEntityId());
        if(event.getData()!=null){
            for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
                map.put(stringObjectEntry.getKey(),stringObjectEntry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));
        messageService.addMessage(message);
    }
    //消费发布事件
    @KafkaListener(topics = {TOPIC_PUBLISH_POST})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误");
        }
        DiscussPost post = discussPostService.selectDiscussPostById(event.getEntityId());
        //存到es中
        elasticSearchService.saveDiscussPost(post);
    }
    //消费发布事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误");
        }
       elasticSearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = {TOPIC_Share})
    public void handleShare(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误");
        }
        String htmlUrl =(String) event.getData().get("htmlUrl");
        String filename=(String) event.getData().get("filename");
        String suffix=(String) event.getData().get("suffix");
        //命令字符串
        String cmd=command+" --quality 75 "+htmlUrl+" "+storage+"/"+filename+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图");
        } catch (IOException e) {
            logger.error("生成长图失败: "+e.getMessage());
        }

        //启用定时器,监视该图片,一旦生成了图片,就上传七牛云
        uploadTask uploadTask=new uploadTask(filename,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(uploadTask, 500);
        uploadTask.setFuture(future);
    }

    class uploadTask implements Runnable{
        //文件名称
        private String filename;
        //名称后缀
        private String suffix;
        //启动任务的返回值,用来停止定时器
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
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
            //生成图片失败
            if(System.currentTimeMillis()-startTime>30000){
                logger.error("执行时间过长,终止任务 "+filename);
                future.cancel(true);
                return;
            }
            //上传失败
            if(count>=6){
                logger.error("上传次数过多,终止任务:" +filename);
                future.cancel(true);
                return;
            }
            String path=storage+"/"+filename+suffix;
            File file=new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s]",++count,filename));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));
                //生成上传凭证
                Auth auth=Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, filename, 3600, policy);
                //指定上传的机房
                UploadManager uploadManager=new UploadManager(new Configuration(Region.huadong()));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    //开始上传图片
                    Response response=uploadManager.put(
                            path,filename,uploadToken,null,"image"+suffix,false
                    );
                    //处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    System.out.println(jsonObject.get("code"));
                    if(jsonObject==null||jsonObject.get("code")==null||!jsonObject.get("code").toString().equals("0")){
                        logger.info(String.format("第%d上传失败[%s]",count,filename));
                    }else{
                        logger.info(String.format("第%d上传成功[%s]",count,filename));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info(String.format("第%d上传失败[%s]",count,filename));
                }
            }else{
                logger.info("等待图片生成 ["+filename+"]");
            }
        }
    }
}
