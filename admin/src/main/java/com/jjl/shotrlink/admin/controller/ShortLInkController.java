package com.jjl.shotrlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jjl.shortlink.project.common.convention.result.Results;
import com.jjl.shortlink.project.dto.req.ShortLInkUpdateReqDTO;
import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.remote.ShortLinkRemoteService;
import com.jjl.shotrlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.jjl.shotrlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/*
短链接控制层
* */
@RestController
@RequiredArgsConstructor
public class ShortLInkController {
    private final ShortLinkRemoteService shortLinkRemoteService;

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> page(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        return shortLinkRemoteService.pageShortLink(shortLinkPageReqDTO);
    }
    @PostMapping("/api/short-link/admin/v1/update")
    public com.jjl.shortlink.project.common.convention.result.Result<Void> updateShortLink(@RequestBody ShortLInkUpdateReqDTO shortLInkUpdateReqDTO) {
        shortLinkRemoteService.updateShortLink(shortLInkUpdateReqDTO);
        return Results.success();
    }

}
