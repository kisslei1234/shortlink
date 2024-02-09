package com.jjl.shotrlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import com.jjl.shotrlink.admin.dao.entity.GroupDO;
import com.jjl.shotrlink.admin.dao.mapper.GroupMapper;
import com.jjl.shotrlink.admin.service.GroupService;
import com.jjl.shotrlink.admin.util.RandomGenerator;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void createGroup(String groupName) {
        String gid = RandomGenerator.generateRandom();
        while (baseMapper.selectById(gid) != null) {
            gid = RandomGenerator.generateRandom();
        }
        GroupDO groupDO = GroupDO.builder().gid(gid)
                .username("admin")
                .name(groupName)
                .build();
        if(baseMapper.insert(groupDO) < 1) {
            throw new ClientException("请不要创建同一个群组");
        }
    }
}
