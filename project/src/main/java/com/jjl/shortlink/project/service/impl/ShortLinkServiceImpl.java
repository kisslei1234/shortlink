package com.jjl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.project.common.convention.exception.ServiceException;
import com.jjl.shortlink.project.dao.entity.ShortLinkDO;
import com.jjl.shortlink.project.dao.mapper.ShortLinkMapper;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import com.jjl.shortlink.project.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String suffix = generateSuffix(shortLinkCreateReqDTO);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(shortLinkCreateReqDTO, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(shortLinkCreateReqDTO.getDomain() + "/" + suffix);
        shortLinkDO.setShortUri(suffix);
        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException e) {
            LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class).eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            if (ObjectUtil.isNotEmpty(baseMapper.selectOne(wrapper))) {
                throw new ServiceException("短链接已存在");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(shortLinkCreateReqDTO.getDomain() + "/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl())
                .gid(shortLinkDO.getGid())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int count = 0;
        String shortUri = "";
        while (true) {
            if (count > 10) {
                throw new ServiceException("生成短链接失败");
            }
            String originUrl = shortLinkCreateReqDTO.getOriginUrl();
            shortUri = HashUtil.hashToBase62(originUrl + System.currentTimeMillis());
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortLinkCreateReqDTO.getDomain() + "/" + shortUri)) {
                break;
            }
            count++;
        }

        return shortUri;
    }
}
