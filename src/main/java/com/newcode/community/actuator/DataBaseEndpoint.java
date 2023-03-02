package com.newcode.community.actuator;

import com.newcode.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/2 15:33
 * @description TODO
 **/
//自定义端点
@Component
@Endpoint(id = "database")
public class DataBaseEndpoint {
    private static final Logger logger= LoggerFactory.getLogger(DataBaseEndpoint.class);

    @Resource
    private DataSource dataSource;

    //get请求访问的端点
    @ReadOperation
    public String check(){
        try (Connection connection = dataSource.getConnection()){
            return CommunityUtil.getJsonString(0,"获取连接成功");
        } catch (SQLException e) {
            logger.error("获取连接失败"+e.getMessage());
            return CommunityUtil.getJsonString(1,"获取连接失败");
        }
    }

}
