package com.newcode.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/1 14:28
 * @description TODO
 **/
@Configuration
public class WkConfig {
    private static final Logger logger= LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        //创建wk图片目录
        File file=new File(wkImageStorage);
        if(!file.exists()){
            file.mkdir();
            logger.info("创建mk图片目录");

        }
    }

}
