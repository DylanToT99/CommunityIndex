package com.newcode.community.util;


/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/23 15:57
 * @description TODO
 **/
public class redisKeyUtil {
    private static final String SPLIT=":";

    private static final String PREFIX_ENTITY_LIKE="like:entity";

    private static final String PREFIX_USER_LIKE="like:user";

    private static final String PREFIX_FOLLOWEE="followee";

    private static final String PREFIX_FOLLOWER="follower";

    private static final String PREFIX_KAPTCHA="kaptcha";

    private static final String PREFIX_TICKET="ticket";

    private static final String PREFIX_User="user";

    private static final String PREFIX_UV="uv";

    private static final String PREFIX_DAU="dau";

    private static final String PREFIX_POST="post";




    //生成某个实体的赞
    //like:entity:entityType:entityId---->set(userId)
    public static String getEntityLike(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //某个用户的赞
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    /**
     * 关注的行为只能发生在用户身上,但是关注的对象可以是其他用户或者帖子(关注帖子默认为收藏帖子)
     * 帖子实体type为1,用户实体为3
     */

    //某个用户关注的实体(关注了谁)            实体的id   关注的时间
    //      用户的id    关注的实体类型--->zset(entityId,nowDate)
    //followee:userId:entityType
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    //某个实体的关注者,粉丝(谁关注了我)
    //follower:entityId:entityType---->zset(userId,nowdate)
    public static String getFollowerKey(int entityId,int entityType){
        return PREFIX_FOLLOWER+SPLIT+entityId+SPLIT+entityType;
    }

    //验证码的key
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_User+SPLIT+userId;
    }

    //单日uv的key
    public static String getUVKey(String date){
        return PREFIX_UV+SPLIT+date;
    }
    //区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV+SPLIT+startDate+SPLIT+endDate;
    }
    //单日活跃用户key
    public static String getDAUKey(String date){
        return PREFIX_DAU+SPLIT+date;
    }
    //活跃区间用户
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU+SPLIT+startDate+SPLIT+endDate;
    }

    //返回统计帖子分数的key
    public static String getPostScoreKey(){
        return PREFIX_POST+SPLIT+"score";
    }

}
