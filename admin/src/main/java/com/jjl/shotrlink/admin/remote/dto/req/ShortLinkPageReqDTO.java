package com.jjl.shotrlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class ShortLinkPageReqDTO extends Page {
    /*
    * 分组id
    * */
    private String gid;
}
