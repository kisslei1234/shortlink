package com.jjl.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* 短链接分组查询返回*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkCountQueryRespDTO {
    private String gid;
    private Integer shortLinkCount;
}
