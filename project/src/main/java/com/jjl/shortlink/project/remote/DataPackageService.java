package com.jjl.shortlink.project.remote;

import com.jjl.shortlink.project.common.convention.result.Result;
import com.jjl.shortlink.project.dao.entity.DataPackageDO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.PriorityQueue;

@FeignClient(value = "short-link-dataPackage", url = "${aggregation.remote-url:}")
public interface DataPackageService {
    @PostMapping("/api/short-link/admin/v1/datapackage")
    Result<DataPackageDO> createDataPackage(@RequestParam("username") String username) ;
    
}
