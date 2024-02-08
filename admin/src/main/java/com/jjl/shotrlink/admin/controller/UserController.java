package com.jjl.shotrlink.admin.controller;

import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.convention.result.Results;
import com.jjl.shotrlink.admin.dto.req.UserRegisterDto;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import com.jjl.shotrlink.admin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * Get user by username
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDto> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }
    @GetMapping("/api/short-link/v1/user/has-username/{username}")
    public Result<Boolean> hasUserByUsername(@PathVariable("username") String username) {
        return userService.hasUserByUsername(username) ? Results.success(true) : Results.success(false);
    }
    @PostMapping("/api/short-link/v1/user/register")
    public Result<Void> registerUser(@RequestBody @Valid UserRegisterDto userRegisterDto) {
        userService.registerUser(userRegisterDto);
        return Results.success();
    }
}
