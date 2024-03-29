package com.jjl.shotrlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jjl.shotrlink.admin.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description t_group
 * @date 2024-02-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_group")
public class GroupDO extends BaseDO {

    /**
    * id
    */
    @TableId
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
    * 删除标识 0：未删除 1：已删除
    */
    private int delFlag;
}