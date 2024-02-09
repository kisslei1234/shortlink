package com.jjl.shotrlink.admin.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginReqDto {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
