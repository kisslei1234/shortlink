
package com.jjl.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接聚合应用
 
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
        "com.jjl.shortlink.admin",
        "com.jjl.shortlink.project"
})
@MapperScan(value = {
        "com.jjl.shortlink.project.dao.mapper",
        "com.jjl.shortlink.admin.dao.mapper"
})
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}