package com.jjl.shotrlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjl.shotrlink.admin.dao.entity.UserDO;
import com.jjl.shotrlink.admin.dto.req.UserRegisterDto;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<UserDO> {
    public UserRespDto getUserByUsername(String username);
    Boolean hasUserByUsername(String username);
    void registerUser(UserRegisterDto userRegisterDto);
}
