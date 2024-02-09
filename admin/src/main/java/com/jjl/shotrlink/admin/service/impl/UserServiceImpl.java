package com.jjl.shotrlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shotrlink.admin.convention.exception.ClientException;
import com.jjl.shotrlink.admin.dao.entity.UserDO;
import com.jjl.shotrlink.admin.dao.mapper.UserMapper;
import com.jjl.shotrlink.admin.dto.req.UserLoginReqDto;
import com.jjl.shotrlink.admin.dto.req.UserRegisterDto;
import com.jjl.shotrlink.admin.dto.req.UserUpdateDto;
import com.jjl.shotrlink.admin.dto.resp.UserLoginRespDto;
import com.jjl.shotrlink.admin.dto.resp.UserRespDto;
import com.jjl.shotrlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.jjl.shotrlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER;
import static com.jjl.shotrlink.admin.common.constant.RedisCacheConstant.LOGIN_PREFIX;
import static com.jjl.shotrlink.admin.convention.errorcode.BaseErrorCode.USER_NAME_EXIST_ERROR;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> rBloomFilter;
    private final RedissonClient redisson;
    private final StringRedisTemplate stringRedisTemplate;

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

    @Override
    public void update(UserUpdateDto userUpdateDto) {
        //TODO 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, userUpdateDto.getUsername());
        baseMapper.update(BeanUtil.toBean(userUpdateDto, UserDO.class), wrapper);
    }

    @Override
    public UserLoginRespDto login(UserLoginReqDto userLoginReqDto) {
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userLoginReqDto.getUsername())
                .eq(UserDO::getPassword, userLoginReqDto.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtil.isEmpty(userDO)) {
            throw new ClientException("用户名或密码错误!");
        }

        //生成token
        String token = UUID.fastUUID().toString();
        if (ObjectUtil.equal(Boolean.TRUE, checkLogin(token, userLoginReqDto.getUsername()))) {
            throw new ClientException("用户已登录");
        }
        stringRedisTemplate.opsForHash().put(LOGIN_PREFIX + userLoginReqDto.getUsername(), token, JSONUtil.toJsonStr(userDO));
        stringRedisTemplate.expire(LOGIN_PREFIX + userLoginReqDto.getUsername(), 30L, TimeUnit.MINUTES);
        return UserLoginRespDto.builder().token(token).build();
    }

    @Override
    public Boolean checkLogin(String token, String username) {
        return stringRedisTemplate.hasKey(LOGIN_PREFIX + username);
    }
}
