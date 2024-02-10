package com.jjl.shotrlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.jjl.shotrlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ShortLinkRemoteService {
    /**
     * 分页查询短链接
     *
     * @param reqDTO
     * @return
     */
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO reqDTO) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", reqDTO.getGid());
        requestMap.put("current", reqDTO.getCurrent());
        requestMap.put("size", reqDTO.getSize());
        String resultPageStr = HttpUtil.get("http://localhost:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


}
