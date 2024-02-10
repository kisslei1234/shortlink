package com.jjl.shortlink.admin.test;

public class UserTableShardingTest {
    public static final String SQL = "CREATE TABLE `t_link_%d` (\n" +
            "  `id` bigint NOT NULL,\n" +
            "  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '域名',\n" +
            "  `short_uri` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链接',\n" +
            "  `full_short_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '完整短链接',\n" +
            "  `origin_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原始链接',\n" +
            "  `enable_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '启用标识，0：启用，1：不启用',\n" +
            "  `gid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组id',\n" +
            "  `click_number` int unsigned NOT NULL DEFAULT '0' COMMENT '点击量',\n" +
            "  `created_type` tinyint(1) DEFAULT NULL COMMENT '创建类型，0:接口创建，1：控制台创建',\n" +
            "  `vaild_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型，0：永久有效，1：自定义',\n" +
            "  `vaild_date` datetime DEFAULT NULL COMMENT '有效期',\n" +
            "  `describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
            "  `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识,0:未删除,1:已删除',\n" +
            "  `favicon` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '网站图标',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `uk_full_short_url` (`full_short_url`) USING BTREE\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;";

    public static void main(String[] args) {
        for (int i = 0;i<5;i++){
            System.out.printf((SQL) + "%n",i);
        }
    }
}
