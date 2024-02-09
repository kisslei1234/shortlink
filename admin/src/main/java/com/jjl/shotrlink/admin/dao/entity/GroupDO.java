package com.jjl.shotrlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description t_group
 * @date 2024-02-09
 */
@Data
@TableName("t_group")
public class GroupDO {

    @TableId(type = IdType.AUTO)
    /**
    * id
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 分组名称
    */
    private String name;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
    * 分组排序
    */
    private Integer sortOrder;

    /**
    * 创建时间
    */
    private LocalDateTime createTime;

    /**
    * 修改时间
    */
    private LocalDateTime updateTime;

    /**
    * 删除标识 0：未删除 1：已删除
    */
    private int delFlag;
}