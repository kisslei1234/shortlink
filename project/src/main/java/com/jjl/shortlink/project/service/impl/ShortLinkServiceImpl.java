package com.jjl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.project.common.convention.exception.ServiceException;
import com.jjl.shortlink.project.dao.entity.ShortLinkDO;
import com.jjl.shortlink.project.dao.mapper.ShortLinkMapper;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import com.jjl.shortlink.project.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        IPage<ShortLinkDO> pageReqDTO = baseMapper.selectPage(shortLinkPageReqDTO, wrapper);
        return pageReqDTO.convert(e -> BeanUtil.toBean(e, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> gidList) {
        QueryWrapper<ShortLinkDO> wrapper = Wrappers.query(ShortLinkDO.builder().build())
                .select("gid", "count(*) as shortLinkCount")
                .in("gid", gidList)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);
        return maps.stream()
                .map(e -> BeanUtil.toBean(e, ShortLinkCountQueryRespDTO.class))
                .collect(Collectors.toList());
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
