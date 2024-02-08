package com.jjl.shotrlink.admin.controller;

import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.convention.result.Results;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import com.jjl.shotrlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * Get user by username
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDto> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }
    @GetMapping("/api/shortlink/v1/user/has-username/{username}")
    public Result<Boolean> hasUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.hasUserByUsername(username));
    }
}
