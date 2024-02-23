
package com.jjl.shortlink.project.initialize;

import com.jjl.shortlink.project.common.constant.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 初始化短链接监控消息队列消费者组

 */
@Component
@RequiredArgsConstructor
public class ShortLinkStatsStreamInitializeTask implements InitializingBean {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        Boolean hasKey = stringRedisTemplate.hasKey(RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY);
        if (hasKey == null || !hasKey) {
            stringRedisTemplate.opsForStream().createGroup(RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY, RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY);
        }
    }
}
