package com.newcode.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 17:46
 * @description TODO
 **/
@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
