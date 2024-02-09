package com.jjl.shotrlink.admin.controller;

import com.jjl.shotrlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
/*
短链接分组控制层
* */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

}
