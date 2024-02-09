package com.jjl.shotrlink.admin.dto.req;

import lombok.Data;

@Data
public class GroupUpdateReqDto {
    /*
    分组id
    * */
    private String gid;
    /*
    分组名称
     */
    private String name;
}