package com.newcode.community.controller;

import com.newcode.community.annotation.LoginRequired;
import com.newcode.community.dao.UserMapper;
import com.newcode.community.entity.User;
import com.newcode.community.service.FollowService;
import com.newcode.community.service.LikeService;
import com.newcode.community.service.UserService;
import com.newcode.community.util.CommunityConstant;
import com.newcode.community.util.CommunityUtil;
import com.newcode.community.util.HostHolder;
import com.newcode.community.util.redisKeyUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/20 16:10
 * @description TODO
 **/
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    public static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Value("${qiniu.key.acess}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;




    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model){
        //上传文件名称
        String filename = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap police=new StringMap();
        police.put("returnBody",CommunityUtil.getJsonString(0));
        //上传身份凭证
        Auth auth=Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, filename, 3600, police);
        //String uploadToken = auth.uploadToken(headerBucketName);
        model.addAttribute("uploadToken",uploadToken);
         model.addAttribute("filename",filename);

        return "/site/setting";
    }

    //更新头像路径
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String filename){
        if(StringUtils.isBlank(filename)){
            return CommunityUtil.getJsonString(1,"您还没有选择图片");
        }

        String url= headerBucketUrl+"/"+filename;
        userService.updateHeaderImg(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJsonString(0);
    }


    //废弃
    @LoginRequired
    @PostMapping("/upload")
    //上传头像保存到服务器
    //MultipartFile类型接受文件
    public String uploadHeaderImg(MultipartFile headerImg, Model model){
        if(headerImg==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String originalFilename = headerImg.getOriginalFilename();
        //解析文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }
        if(!(suffix.equals("png")||suffix.equals("jpg")||suffix.equals("jpeg"))){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        String filename = CommunityUtil.generateUUID() +"."+ suffix;
        //确定文件存放的路径
        File dest=new File(uploadPath+"/"+filename);
        try {
            //存储文件
            headerImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败,服务器异常",e);
        }
        //更新当前用户头像的路径(web路径)
        User user = hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+filename;
        userService.updateHeaderImg(user.getId(),headerUrl);
        return "redirect:/index";
    }

    //废弃
    //设置头像的访问路径
    @RequestMapping(path = "header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String filename, HttpServletResponse response){
        //服务器存放的路径:
        filename=uploadPath+"/"+filename;
        //输出图片
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf(".")+1);
        //响应图片
        response.setContentType("image/"+suffix);
        //在try括号里的流,都会在finally里中自动关闭
        try ( FileInputStream fis=new FileInputStream(filename)){
            OutputStream os=response.getOutputStream();

            byte[]buffer=new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }
    //修改密码
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword,String newPassword1,String newPassword2,Model model){
        //获取当前用户
        User user = hostHolder.getUser();
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg","密码不能为空!");
            return "/site/setting";
        }
        //若原密码不正确
        if(!user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt()))){
            model.addAttribute("oldPasswordMsg","原密码错误,请重新输入!");
            return "/site/setting";
        }
        //若新密码为空:
        if(StringUtils.isBlank(newPassword1)){
            model.addAttribute("newPasswordMsg1","新密码不能为空");
            return "/site/setting";
        }
        //若两次输入的密码不一致
        if(!newPassword1.equals(newPassword2)){
            model.addAttribute("newPasswordMsg2","两次输入的密码不一致,请重新输入!");
            return "/site/setting";
        }
        //若新密码和原密码一致:
        if(user.getPassword().equals(CommunityUtil.md5(newPassword1+user.getSalt()))){
            model.addAttribute("newPasswordMsg1","新密码不能与原密码一致!");
            return "/site/setting";
        }
        //加密
        newPassword2=CommunityUtil.md5(newPassword2+user.getSalt());
        userService.updatePassword(user.getId(),newPassword2);
        return "redirect:/logout";
    }
    //个人主页
    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable("userId") int userId,Model model){
        User user= userService.findUserById(userId);
        if(user==null){
            throw new IllegalArgumentException("用户不存在");
        }
        //用户收到的点赞数
        model.addAttribute("user",user);
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",userLikeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注该用户
        boolean hasFollowed=false;
        if (hostHolder.getUser()!=null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

}
