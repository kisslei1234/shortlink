package com.jjl.shotrlink.datapackage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shotrlink.datapackage.entity.DataPackageDO;
import org.springframework.stereotype.Service;

@Service
public interface DataPackageService  extends IService<DataPackageDO> {
     DataPackageDO createDataPackage(String userName);
}
