
package com.jjl.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 短链接应用
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.jjl.shortlink.project.dao.mapper")
@EnableFeignClients("com.jjl.shortlink.project.remote")
public class ShortLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
    }
}
