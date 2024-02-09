package com.jjl.shotrlink.admin.dto.resp;

import lombok.Builder;
import lombok.Data;
/*
用户登录响应
 */
@Data
@Builder
public class UserLoginRespDto {
    /*
    * 用户登录使用的token令牌
    */
    private String token;
}
