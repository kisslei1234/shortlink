package com.jjl.shortlink.datapackage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shortlink.datapackage.dao.entity.DataPackageDO;
import org.springframework.stereotype.Service;

@Service
public interface DataPackageService  extends IService<DataPackageDO> {
     DataPackageDO createDataPackage(String userName);
}
