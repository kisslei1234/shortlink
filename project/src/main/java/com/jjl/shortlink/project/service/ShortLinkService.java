package com.jjl.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shortlink.project.dao.entity.ShortLinkDO;
import com.jjl.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ShortLinkService  extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO);

    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> gidList);

    Void updateShortLink(ShortLinkUpdateReqDTO shortLInkUpdateReqDTO);

    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException;
    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);
}
