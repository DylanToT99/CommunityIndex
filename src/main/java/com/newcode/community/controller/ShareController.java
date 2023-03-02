package com.newcode.community.controller;

import com.newcode.community.entity.Event;
import com.newcode.community.event.EventProducer;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/1 14:32
 * @description TODO
 **/
@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger=LoggerFactory.getLogger(ShareController.class);
    @Resource
    private EventProducer eventProducer;
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String context;

    @Value("${wk.image.storage}")
    private String storage;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @GetMapping("/share")
    @ResponseBody
    //查询参数为(?htmlUrl=www.xxx....)
    public String share(String htmlUrl){
        //文件名
        String filename = CommunityUtil.generateUUID();
        //异步生成长图
        Event event=new Event()
                .setTopic(TOPIC_Share)
                .setData("htmlUrl",htmlUrl)
                .setData("filename",filename)
                .setData("suffix",".png");
        eventProducer.fireEvent(event);

        //返回访问路径
        Map<String,Object>map=new HashMap<>();
        //map.put("setUrl",domain+context+"/share/image/"+filename);
        map.put("setUrl",shareBucketUrl+"/"+filename);
        return CommunityUtil.getJsonString(0,null,map);
    }


    //废弃
    @GetMapping("/share/image/{filename}")
    public void getSetImage(@PathVariable("filename")String filename, HttpServletResponse response)  {
        if(StringUtils.isBlank(filename)){
            throw new IllegalArgumentException("文件名不能为空");
        }
        response.setContentType("image/png");
        File file = new File(storage + "/" + filename + ".png");
        try {
            OutputStream outputStream = response.getOutputStream();
            FileInputStream fis=new FileInputStream(file);
            byte[] buffer=new byte[1024];
            int flag=0;
            while((flag=fis.read(buffer))!=-1){
                outputStream.write(buffer,0,flag);
            }
            
        } catch (IOException e) {
            logger.error("获取长图失败: "+e.getMessage());
        }
    }
}
