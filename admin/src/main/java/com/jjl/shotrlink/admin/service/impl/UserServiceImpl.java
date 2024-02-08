package com.jjl.shotrlink.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import com.jjl.shotrlink.admin.dao.entity.UserDO;
import com.jjl.shotrlink.admin.dao.mapper.UserMapper;
import com.jjl.shotrlink.admin.dto.req.UserRegisterDto;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import com.jjl.shotrlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.jjl.shotrlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER;
import static com.jjl.shotrlink.admin.convention.errorcode.BaseErrorCode.USER_NAME_EXIST_ERROR;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> rBloomFilter;
    private final RedissonClient redisson;

    @Override
    public UserRespDto getUserByUsername(String username) {
        UserDO userDO = this.getOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
        UserRespDto userRespDto = new UserRespDto();
        BeanUtils.copyProperties(userDO, userRespDto);
        return userRespDto;
    }

    @Override
    public Boolean hasUserByUsername(String username) {
        return rBloomFilter.contains(username);
    }

    @Override
    public void registerUser(UserRegisterDto userRegisterDto) {
        if (hasUserByUsername(userRegisterDto.getUsername())) {
            throw new ClientException(USER_NAME_EXIST_ERROR);
        }
        RLock lock = redisson.getLock(LOCK_USER_REGISTER + userRegisterDto.getUsername());
        try {
            if (lock.tryLock()) {
                UserDO userDO = new UserDO();
                BeanUtils.copyProperties(userRegisterDto, userDO);
                this.save(userDO);
                rBloomFilter.add(userRegisterDto.getUsername());
            } else {
                throw new ClientException(USER_NAME_EXIST_ERROR);
            }
        } finally {
            lock.unlock();
        }
    }
}
