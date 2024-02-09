package com.jjl.shotrlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.common.biz.user.UserContext;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import com.jjl.shotrlink.admin.dao.entity.GroupDO;
import com.jjl.shotrlink.admin.dao.mapper.GroupMapper;
import com.jjl.shotrlink.admin.dto.resp.GroupQueryRespDto;
import com.jjl.shotrlink.admin.service.GroupService;
import com.jjl.shotrlink.admin.util.RandomGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void createGroup(String groupName) {
        String gid = RandomGenerator.generateRandom();
        while (baseMapper.selectById(gid) != null) {
            gid = RandomGenerator.generateRandom();
        }
        GroupDO groupDO = GroupDO.builder().gid(gid)
                .username(UserContext.getUsername())
                .name(groupName)
                .sortOrder(0)
                .build();
        if (baseMapper.insert(groupDO) < 1) {
            throw new ClientException("请不要创建同一个群组");
        }
    }

    @Override
    public List<GroupQueryRespDto> getGroups() {
        LambdaQueryWrapper<GroupDO> doLambdaQueryWrapper = Wrappers.<GroupDO>lambdaQuery().eq(GroupDO::getUsername, UserContext.getUsername());
        return BeanUtil.copyToList(baseMapper.selectList(doLambdaQueryWrapper), GroupQueryRespDto.class);
    }
}
