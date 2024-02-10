package com.jjl.shotrlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shotrlink.admin.dao.entity.GroupDO;
import com.jjl.shotrlink.admin.dto.req.GroupSortReqDto;
import com.jjl.shotrlink.admin.dto.req.GroupUpdateReqDto;
import com.jjl.shotrlink.admin.dto.resp.GroupQueryRespDto;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    @NotBlank(message = "群组名不能为空")
    void createGroup(String groupName);

    List<GroupQueryRespDto> getGroups();

    void updateGroup(GroupUpdateReqDto groupUpdateReqDto);

    void deleteGroup(String gid);

    /*
    短链接分组排序
    * */
    void sortGroup(List<GroupSortReqDto> groupSortReqDtos);
}
