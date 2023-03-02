package com.newcode.community;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/2 15:07
 * @description TODO
 **/

import com.newcode.community.entity.DiscussPost;
import com.newcode.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = NewCodeApplication.class)
@RunWith(SpringRunner.class)
public class springbootTest {
    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    //测试启动前被调用一次
    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }
    //测试启动后被调用一次
    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }

    //每个方法之前都会调用
    //测试前创造数据
    @Before
    public void before() {
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    //每个方法之=之后都会调用
    //测试后删除数据
    @After
    public void after() {
        System.out.println("after");
        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.selectDiscussPostById(data.getId());
        //断言
        Assert.assertNotNull(post);
        Assert.assertEquals(data.getTitle(), post.getTitle());
        Assert.assertEquals(data.getContent(), post.getContent());
    }
    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);

        DiscussPost post = discussPostService.selectDiscussPostById(data.getId());
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }
}
