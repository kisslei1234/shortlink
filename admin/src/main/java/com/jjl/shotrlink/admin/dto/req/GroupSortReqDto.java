package com.jjl.shotrlink.admin.dto.req;

import lombok.Data;

/*
短链接排序分组请求参数
* */
@Data
public class GroupSortReqDto {
    private String gid;

    private Integer sortOrder;
}
