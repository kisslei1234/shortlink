package com.jjl.shotrlink.admin.controller;

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
    public UserRespDto getUserByUsername(@PathVariable("username") String username) {
        return userService.getUserByUsername(username);
    }
}
