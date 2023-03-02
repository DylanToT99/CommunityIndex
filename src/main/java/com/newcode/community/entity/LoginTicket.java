package com.newcode.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/19 14:36
 * @description TODO
 **/
@Data
public class LoginTicket {
    private int id;
    private int userId;
    //1为删除 0为正常使用
    private int status;
    //过期时间
    private Date expired;
    //标识
    private String ticket;
}
