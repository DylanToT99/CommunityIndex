package com.newcode.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 19:42
 * @description TODO
 **/
@Data
public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}
