package com.newcode.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 15:31
 * @description TODO
 **/
@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class redisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test(){
        //String redisKey="test:count";
        //redisTemplate.opsForValue().set(redisKey,1);
        //System.out.println(redisTemplate.opsForValue().get(redisKey));
        //
        //System.out.println(redisTemplate.opsForValue().increment(redisKey));
        //System.out.println(redisTemplate.opsForValue().decrement(redisKey));

        String testHash="test:hash1";
        redisTemplate.opsForHash().put(testHash,"id",1);
        redisTemplate.opsForHash().put(testHash,"username","张三");
        redisTemplate.opsForHash().get(testHash,"username");
    }

    //编程式事务
    @Test
    public void testTransactional(){
        Object execute = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();
                operations.opsForSet().add(redisKey, "张三");
                operations.opsForSet().add(redisKey, "李四");
                operations.opsForSet().add(redisKey, "王五");
                System.out.println(operations.opsForSet().members(redisKey));
                return operations.exec();
            }
        });
        System.out.println(execute);
    }

    //多次对一个key操作
    @Test
    public void testBound(){
        String redisKey="test:bound1";
        redisTemplate.opsForValue().set(redisKey,1);
        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
    }


    @Test
    public void test1(){
        stringRedisTemplate.opsForValue().set("test:count1","2");
    }

    @Test
    public void hyperLog(){
       String[]values= new String[1000];
        int j=0;
        for (int i = 0; i < 1000000; i++) {
            j=i%1000;
            values[j]="userTest_"+i;
            if(j==999){
                redisTemplate.opsForHyperLogLog().add("hpl",values);
            }
        }
        Long hpl = redisTemplate.opsForHyperLogLog().size("hpl");
        System.out.println(hpl);
    }
}
