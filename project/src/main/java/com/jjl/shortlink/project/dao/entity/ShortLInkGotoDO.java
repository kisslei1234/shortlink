package com.jjl.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("t_link_goto")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLInkGotoDO {
    @TableId
    private Long id;
    private String gid;
    private String fullShortUrl;
}
