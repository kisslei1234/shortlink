package com.jjl.shortlink.datapackage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.jjl.shortlink.datapackage.dao.mapper")
public class DataPackageApplication {
    public static void main(String[] args) {
        Arrays.asList(new int[]{1,23,4});
        SpringApplication.run(DataPackageApplication.class, args);
    }
}
