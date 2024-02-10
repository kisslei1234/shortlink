package com.jjl.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jjl.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @description t_link
 * @author zhengkai.blog.csdn.net
 * @date 2024-02-10
 */
@Data
@TableName("t_link")
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkDO extends BaseDO {



    /**
    * id
    */
    @TableId(type = IdType.ASSIGN_ID)
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
    * 启用标识，0：启用，1：不启用
    */
    private int enableStatus;

    /**
    * 分组id
    */
    private String gid;

    /**
    * 点击量
    */
    private Integer clickNumber;

    /**
    * 创建类型，0:接口创建，1：控制台创建
    */
    private int createdType;

    /**
    * 有效期类型，0：永久有效，1：自定义
    */
    private int vaildDateType;

    /**
    * 有效期
    */
    private LocalDateTime vaildDate;

    /**
    * 描述
    */
    @TableField(value = "`describe`")
    private String describe;

    /**
    * 删除标识，0:未删除，1:已删除
    */
    private int delFlag;
    /*
    * 网站图标
    * */
    private String favicon;
}