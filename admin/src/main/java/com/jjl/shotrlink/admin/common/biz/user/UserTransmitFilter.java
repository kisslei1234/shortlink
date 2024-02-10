package com.jjl.shotrlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jjl.shotrlink.admin.common.constant.RedisCacheConstant.LOGIN_PREFIX;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
@Slf4j
public class UserTransmitFilter implements Filter {
    private final StringRedisTemplate stringRedisTemplate;

    // 需要排除的URL列表
    private final List<String> excludedUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 添加需要排除的URL
        excludedUrls.add("/api/short-link/v1/user/register");
        excludedUrls.add("/api/short-link/v1/user/login");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String requestUrl = httpServletRequest.getRequestURI();

            // 检查请求的URL是否需要排除
            if (isExcludedUrl(requestUrl)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            String token = httpServletRequest.getHeader("token");
            if (!StringUtils.hasText(token)) {
                throw new ClientException("请先登录");
            }
            String userName = httpServletRequest.getHeader("username");
            Object userJson = stringRedisTemplate.opsForHash().get(LOGIN_PREFIX + userName, token);
            if (Objects.isNull(userJson)) {
                throw new ClientException("请先登录");
            }
            UserInfoDTO userInfoDTO = JSON.parseObject(userJson.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
                filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }

    }

    @Override
    public void destroy() {
        // 清空排除的URL列表
        excludedUrls.clear();

    }

    private boolean isExcludedUrl(String url) {
        // 检查请求的URL是否在排除的URL列表中
        return excludedUrls.contains(url);
    }
}