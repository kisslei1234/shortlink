package com.jjl.shortlink.project.common.constant;
/*
* redis常量类
* */
public class RedisKeyConstant {
    /*
    * 短链接跳转前缀key
    * */
    public static final String SHORT_LINK_GOTO = "short_link_goto_%s";
    public static final String SHORT_LINK_GOTO_LOCK = "short_link_goto_lock_%s";
    public static final String SHORT_LINK_GOTO_ISNULL = "short_link_goto_isnull_%s";
    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock:update-gid:%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";

    /**
     * 短链接统计判断是否新用户缓存标识
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv:";

    /**
     * 短链接统计判断是否新 IP 缓存标识
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "short-link:stats:uip:";

    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats-stream:only-group";

}
