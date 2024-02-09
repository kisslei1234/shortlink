package com.jjl.shotrlink.admin.controller;

import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.convention.result.Results;
import com.jjl.shotrlink.admin.dto.req.GroupSaveReqDto;
import com.jjl.shotrlink.admin.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
/*
短链接分组控制层
* */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> createGroup(@RequestBody @Valid GroupSaveReqDto groupSaveReqDto) {
        groupService.createGroup(groupSaveReqDto.getName());
        return Results.success();
    }
}
