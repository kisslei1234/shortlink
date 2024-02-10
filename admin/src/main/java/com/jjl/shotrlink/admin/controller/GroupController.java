package com.jjl.shotrlink.admin.controller;

import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.convention.result.Results;
import com.jjl.shotrlink.admin.dto.req.GroupSaveReqDto;
import com.jjl.shotrlink.admin.dto.req.GroupSortReqDto;
import com.jjl.shotrlink.admin.dto.req.GroupUpdateReqDto;
import com.jjl.shotrlink.admin.dto.resp.GroupQueryRespDto;
import com.jjl.shotrlink.admin.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
短链接分组控制层
* */
@RestController
@RequiredArgsConstructor
@Slf4j
public class GroupController {
    private final GroupService groupService;
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> createGroup(@RequestBody @Valid GroupSaveReqDto groupSaveReqDto) {
        groupService.createGroup(groupSaveReqDto.getName());
        return Results.success();
    }
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<GroupQueryRespDto>> getGroups() {
        return Results.success(groupService.getGroups());
    }
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody @Valid GroupUpdateReqDto groupUpdateReqDto) {
        groupService.updateGroup(groupUpdateReqDto);
        return Results.success();
    }
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortReqDto> groupSortReqDtos) {
        groupService.sortGroup(groupSortReqDtos);
        return Results.success();
    }

}
