package com.jjl.shotrlink.admin.common.biz.user;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jjl.shotrlink.admin.common.constant.RedisCacheConstant.LOGIN_PREFIX;
import static com.jjl.shotrlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;

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
        excludedUrls.add("/api/short-link/admin/v1/user/register");
        excludedUrls.add("/api/short-link/admin/v1/user/login");
        excludedUrls.add("/api/short-link/admin/v1/user/has-username/*");
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
            String userName = httpServletRequest.getHeader("username");
            if (!StrUtil.isAllNotBlank(token, userName)) {
                throw new ClientException(USER_TOKEN_FAIL);
            }
            Object userJsonObject = stringRedisTemplate.opsForHash().get(LOGIN_PREFIX + userName, token);
            if (ObjectUtil.isNull(userJsonObject)) {
                throw new ClientException(USER_TOKEN_FAIL);
            }
            UserInfoDTO userInfoDTO;
            userInfoDTO = JSON.parseObject(userJsonObject.toString(), UserInfoDTO.class);
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