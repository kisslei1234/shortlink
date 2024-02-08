package com.jjl.shotrlink.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.dao.entity.UserDO;
import com.jjl.shotrlink.admin.dao.mapper.UserMapper;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import com.jjl.shotrlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Override
    public UserRespDto getUserByUsername(String username) {
        UserDO userDO = this.getOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
        UserRespDto userRespDto = new UserRespDto();
        BeanUtils.copyProperties(userDO, userRespDto);
        return userRespDto;
    }
}
