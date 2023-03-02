package com.newcode.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/22 18:32
 * @description TODO
 **/
@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
