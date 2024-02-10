package com.jjl.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkPageRespDTO{

    /**
     * id
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组id
     */
    private String gid;

    /**
     * 有效期类型，0：永久有效，1：自定义
     */
    private int vaildDateType;

    /**
     * 有效期
     */
    private LocalDateTime vaildDate;

    /*
     * 网站图标
     * */
    private String favicon;

}
