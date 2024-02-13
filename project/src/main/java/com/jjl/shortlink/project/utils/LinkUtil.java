package com.jjl.shortlink.project.utils;

import jakarta.servlet.http.HttpServletRequest;

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
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }
    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
}
