package com.jjl.shortlink.project.controller;

import com.jjl.shortlink.project.common.convention.result.Result;
import com.jjl.shortlink.project.common.convention.result.Results;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
