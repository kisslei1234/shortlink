
package com.jjl.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jjl.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.jjl.shortlink.admin.common.convention.result.Result;
import com.jjl.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;

/**
 * URL 回收站接口层

 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
