package com.newcode.community.controller;

import com.newcode.community.util.redisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/28 14:59
 * @description TODO
 **/
@Service
public class DataService {
    @Resource
    private RedisTemplate redisTemplate;

    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");

    //将指定ip计入UV
    public void recordUV(String ip){
        String redisKey = redisKeyUtil.getUVKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    //统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //整理日期范围内的key
        List<String>keyList=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String uvKey = redisKeyUtil.getUVKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE,1);
        }
        //合并这些数据
        if (keyList.isEmpty()){
            return 0;
        }
        String uvKey = redisKeyUtil.getUVKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey, keyList.toArray());

        //返回统计的数据大小
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }
    //将指定用户计入DAU
    public void recordDAU(int userId){
        String dauKey = redisKeyUtil.getDAUKey(simpleDateFormat.format(new Date()));
        //用户id是多少,就把多少位设置为1
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }
    //指定日期范围内的DAU
    public long calculateDAU(Date start,Date end){
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //整理日期范围内的key
        List<byte[]>keyList=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String dauKey = redisKeyUtil.getDAUKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(dauKey.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //进行运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String dauKey = redisKeyUtil.getDAUKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
                //进行or运算
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(dauKey.getBytes());
            }
        });
    }


}
