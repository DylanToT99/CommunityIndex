package com.newcode.community.dao;

import com.newcode.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 17:45
 * @description TODO
 **/
@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id,int status);

    int updateHeaderUrl(int id,String headerUrl);

    int updatePassword(int id,String password);
}
