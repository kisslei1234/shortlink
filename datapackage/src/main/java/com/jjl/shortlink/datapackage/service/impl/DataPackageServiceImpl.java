package com.jjl.shortlink.datapackage.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.datapackage.dao.entity.DataPackageDO;
import com.jjl.shortlink.datapackage.dao.mapper.DataPackageMapper;
import com.jjl.shortlink.datapackage.service.DataPackageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataPackageServiceImpl extends ServiceImpl<DataPackageMapper, DataPackageDO> implements DataPackageService {
    @Override
    public DataPackageDO createDataPackage(String userName) {
        DataPackageDO dataPackageDO = DataPackageDO.builder().expirationType(0)
                .ownerName(userName)
                .expires(null)
                .isDeleted(0)
                .type(0)
                .remain(100L)
                .packageId(UUID.fastUUID().toString())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        baseMapper.insert(dataPackageDO);
        return dataPackageDO;
    }
}
