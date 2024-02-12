package com.jjl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.project.common.convention.exception.ServiceException;
import com.jjl.shortlink.project.common.enums.VailDateTypeEnum;
import com.jjl.shortlink.project.dao.entity.ShortLInkGotoDO;
import com.jjl.shortlink.project.dao.entity.ShortLinkDO;
import com.jjl.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.jjl.shortlink.project.dao.mapper.ShortLinkMapper;
import com.jjl.shortlink.project.dto.req.ShortLInkUpdateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import com.jjl.shortlink.project.utils.HashUtil;
import com.jjl.shortlink.project.utils.LinkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jjl.shortlink.project.common.constant.RedisKeyConstant.*;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String suffix = generateSuffix(shortLinkCreateReqDTO);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(shortLinkCreateReqDTO, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(shortLinkCreateReqDTO.getDomain() + "/" + suffix);
        shortLinkDO.setShortUri(suffix);
        ShortLInkGotoDO shortLInkGotoDO = BeanUtil.toBean(shortLinkDO, ShortLInkGotoDO.class);
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLInkGotoDO);
        } catch (DuplicateKeyException e) {
            LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class).eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            if (ObjectUtil.isNotEmpty(baseMapper.selectOne(wrapper))) {
                throw new ServiceException("短链接已存在");
            }
        }
        stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO, shortLinkDO.getFullShortUrl()), shortLinkCreateReqDTO.getOriginUrl(), LinkUtil.getLinkCacheValidDate(shortLinkCreateReqDTO.getValidDate()), TimeUnit.SECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(shortLinkCreateReqDTO.getDomain() + "/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://"+shortLinkDO.getFullShortUrl())
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void updateShortLink(ShortLInkUpdateReqDTO shortLInkUpdateReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, shortLInkUpdateReqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtil.isEmpty(hasShortLinkDO)) {
            throw new ServiceException("短链接不存在");
        }
        if (ObjectUtil.equal(hasShortLinkDO.getGid(), shortLInkUpdateReqDTO.getGid())) {
            BeanUtil.copyProperties(shortLInkUpdateReqDTO, hasShortLinkDO);
            LambdaUpdateWrapper<ShortLinkDO> set = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLInkUpdateReqDTO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, shortLInkUpdateReqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(ObjectUtil.equals(shortLInkUpdateReqDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(hasShortLinkDO, set);
        } else {
            baseMapper.deleteById(hasShortLinkDO);
            BeanUtil.copyProperties(shortLInkUpdateReqDTO, hasShortLinkDO);
            if (ObjectUtil.equal(shortLInkUpdateReqDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType())) {
                hasShortLinkDO.setValidDate(null);
            }
            baseMapper.insert(hasShortLinkDO);
        }
        return null;
    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String fullShortUrl = serverName+":"+serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            response.sendRedirect(originalLink);
            return;
        }
        if (ObjectUtil.equals(Boolean.FALSE, shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl))) {
            response.sendRedirect("/page/notfound");
            return;
        }
        //判断是否缓存穿透
        String nullUrl = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO_ISNULL, fullShortUrl));
        if (StrUtil.isNotBlank(nullUrl)) {
            response.sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(SHORT_LINK_GOTO_LOCK, fullShortUrl));
        lock.lock();
        try {
            originalLink = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                response.sendRedirect(originalLink);
                return;
            }
            //查询数据库
            LambdaQueryWrapper<ShortLInkGotoDO> wrapper = Wrappers.lambdaQuery(ShortLInkGotoDO.class)
                    .eq(ShortLInkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLInkGotoDO shortLInkGotoDO = shortLinkGotoMapper.selectOne(wrapper);
            //短链接不存在
            if (ObjectUtil.isEmpty(shortLInkGotoDO)) {
                stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLInkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (ObjectUtil.isEmpty(shortLinkDO)) {
                response.sendRedirect("/page/notfound");
                return;
            }
            if (ObjectUtil.equal(shortLinkDO.getEnableStatus(), 1)) {
                response.sendRedirect("/page/notfound");
                return;
            }
            if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().isBefore(LocalDateTime.now())) {
                shortLinkDO.setEnableStatus(1);
                baseMapper.updateById(shortLinkDO);
                response.sendRedirect("/page/notfound");
                return;
            }
            originalLink = shortLinkDO.getOriginUrl();
            stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO, fullShortUrl), originalLink, LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()), TimeUnit.SECONDS);
            response.sendRedirect(originalLink);
        } finally {
            lock.unlock();
        }

    }


    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int count = 0;
        String shortUri;
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
