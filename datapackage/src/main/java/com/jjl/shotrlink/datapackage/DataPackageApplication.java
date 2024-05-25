package com.jjl.shotrlink.datapackage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jjl.shotrlink.datapackage.dao.mapper")
public class DataPackageApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataPackageApplication.class, args);
    }
}
