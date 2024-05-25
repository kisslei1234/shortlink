package com.jjl.shotrlink.datapackage.controller;

import com.jjl.shotrlink.datapackage.convention.result.Result;
import com.jjl.shotrlink.datapackage.convention.result.Results;
import com.jjl.shotrlink.datapackage.entity.DataPackageDO;
import com.jjl.shotrlink.datapackage.service.DataPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DataPackageController {
    private final DataPackageService dataPackageService;
    @PostMapping("/api/short-link/admin/v1/datapackage")
    public Result<DataPackageDO> createDataPackage(@RequestParam("username") String username) {
        DataPackageDO dataPackage = dataPackageService.createDataPackage(username);
        return Results.success(dataPackage);
    }
}
