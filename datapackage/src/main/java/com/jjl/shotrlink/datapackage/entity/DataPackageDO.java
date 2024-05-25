package com.jjl.shotrlink.datapackage.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_package")
public class DataPackageDO {
    @TableId
    private Long id;
    private String packageId;
    private String ownerName;
    private Integer type;
    private Long remain;
    private Integer expirationType;
    private LocalDateTime expires;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

}
