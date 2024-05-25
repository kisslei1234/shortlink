package com.jjl.shotrlink.datapackage.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.datapackage.entity.DataPackageDO;
import com.jjl.shotrlink.datapackage.mapper.DataPackageMapper;
import com.jjl.shotrlink.datapackage.service.DataPackageService;
import org.springframework.stereotype.Service;

@Service
public class DataPackageServiceImpl extends ServiceImpl<DataPackageMapper, DataPackageDO> implements DataPackageService {
    @Override
    public DataPackageDO createDataPackage(String userName) {
        DataPackageDO dataPackageDO = DataPackageDO.builder().expirationType(0)
                .ownerName(userName)
                .expires(null)
                .isDeleted(0)
                .packageId(UUID.fastUUID().toString())
                .build();
        baseMapper.insert(dataPackageDO);
        return dataPackageDO;
    }
}
