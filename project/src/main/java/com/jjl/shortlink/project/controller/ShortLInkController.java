package com.jjl.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jjl.shortlink.project.common.convention.result.Result;
import com.jjl.shortlink.project.common.convention.result.Results;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
短链接控制层
* */
@RestController
@RequiredArgsConstructor
public class ShortLInkController {
    private final ShortLinkService shortLinkService;
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> CreateShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        return Results.success(shortLinkService.createShortLink(shortLinkCreateReqDTO));
    }
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> page(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gid") List<String> gidList){
        return Results.success(shortLinkService.listGroupShortLinkCount(gidList));
    }
}
