package com.jjl.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shortlink.project.dao.entity.ShortLinkDO;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

public interface ShortLinkService  extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO);
}
