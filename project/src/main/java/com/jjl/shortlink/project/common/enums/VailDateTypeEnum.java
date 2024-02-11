package com.jjl.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
有效期类型
* */
@RequiredArgsConstructor
public enum VailDateTypeEnum {
    PERMANENT(0, "永久有效"),
    CUSTOM(1, "自定义");
    @Getter
    private final int type;
    private final String desc;
}
