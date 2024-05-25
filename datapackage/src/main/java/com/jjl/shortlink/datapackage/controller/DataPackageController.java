package com.jjl.shortlink.datapackage.controller;

import com.jjl.shortlink.datapackage.dao.entity.DataPackageDO;
import com.jjl.shortlink.datapackage.convention.result.Result;
import com.jjl.shortlink.datapackage.convention.result.Results;
import com.jjl.shortlink.datapackage.service.DataPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DataPackageController {
    @Autowired
    private  DataPackageService dataPackageService;
    @PostMapping("/api/short-link/admin/v1/datapackage")
    public Result<DataPackageDO> createDataPackage(@RequestParam("username") String username)  {
        throw new RuntimeException();
       // DataPackageDO dataPackage = dataPackageService.createDataPackage(username);
    //    return Results.success(dataPackage);
    }
}
