package com.jjl.shotrlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.common.biz.user.UserContext;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import com.jjl.shotrlink.admin.convention.result.Result;
import com.jjl.shotrlink.admin.dao.entity.GroupDO;
import com.jjl.shotrlink.admin.dao.mapper.GroupMapper;
import com.jjl.shotrlink.admin.dto.req.GroupSortReqDto;
import com.jjl.shotrlink.admin.dto.req.GroupUpdateReqDto;
import com.jjl.shotrlink.admin.dto.resp.GroupQueryRespDto;
import com.jjl.shotrlink.admin.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shotrlink.admin.remote.ShortLinkRemoteService;
import com.jjl.shotrlink.admin.service.GroupService;
import com.jjl.shotrlink.admin.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    private final ShortLinkRemoteService shortLinkRemoteService;

    @Override
    public void createGroup(String groupName) {
    createGroup(groupName, UserContext.getUsername());
    }

    @Override
    public void createGroup(String groupName, String username) {
        String gid = RandomGenerator.generateRandom();
        while (baseMapper.selectById(gid) != null) {
            gid = RandomGenerator.generateRandom();
        }
        GroupDO groupDO = GroupDO.builder().gid(gid)
                .username(username)
                .name(groupName)
                .sortOrder(0)
                .build();
        if (baseMapper.insert(groupDO) < 1) {
            throw new ClientException("请不要创建同一个群组");
        }
    }

    @Override
    public List<GroupQueryRespDto> getGroups() {
        LambdaQueryWrapper<GroupDO> doLambdaQueryWrapper = Wrappers.<GroupDO>lambdaQuery()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByAsc(GroupDO::getSortOrder);
        List<GroupDO> groupDOList = baseMapper.selectList(doLambdaQueryWrapper);
        List<String> stringList = groupDOList
                .stream()
                .map(GroupDO::getGid).toList();
        Result<List<ShortLinkCountQueryRespDTO>> listResult = shortLinkRemoteService.listGroupShortLinkCount(stringList);
        List<ShortLinkCountQueryRespDTO> data = listResult.getData();
        Map<String, Integer> collect = data.stream().collect(Collectors.toMap(ShortLinkCountQueryRespDTO::getGid, ShortLinkCountQueryRespDTO::getShortLinkCount));
        List<GroupQueryRespDto> groupQueryRespDtos = BeanUtil.copyToList(groupDOList, GroupQueryRespDto.class);
        groupQueryRespDtos.forEach(e -> {
            e.setShortLinkCount(collect.get(e.getGid()) == null ? 0 : collect.get(e.getGid()));
        });
        return groupQueryRespDtos;
    }

    @Override
    public void updateGroup(GroupUpdateReqDto groupUpdateReqDto) {
        if (baseMapper.update(GroupDO.builder().name(groupUpdateReqDto.getName()).build(), Wrappers.<GroupDO>lambdaUpdate()
                .eq(GroupDO::getGid, groupUpdateReqDto.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)) < 1) {
            throw new ClientException("更新失败");
        }
    }

    @Override
    public void deleteGroup(String gid) {
        if (baseMapper.update(GroupDO.builder().delFlag(1).build(), Wrappers.<GroupDO>lambdaUpdate()
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)) < 1) {
            throw new ClientException("删除失败");
        }
    }

    @Override
    public void sortGroup(List<GroupSortReqDto> groupSortReqDtos) {
        groupSortReqDtos.forEach(e -> {
            GroupDO groupDO = GroupDO.builder().sortOrder(e.getSortOrder()).build();
            LambdaUpdateWrapper<GroupDO> wrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, e.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            if (baseMapper.update(groupDO, wrapper) < 1) {
                throw new ClientException("排序失败");
            }
        });
    }
}
