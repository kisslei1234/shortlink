package com.jjl.shortlink.project.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.jjl.shortlink.project.common.constant.ShortLinkConstant.FOREVER_DEFAULT_EXPIRE_TIME;

/*
短链接工具类
* */
public class LinkUtil {
    public static long getLinkCacheValidDate(LocalDateTime validDate) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = Optional.ofNullable(validDate)
                .map(date -> Duration.between(now, date).getSeconds())
                .orElse(FOREVER_DEFAULT_EXPIRE_TIME);
        return Math.max(seconds, 0L);
    }
}
