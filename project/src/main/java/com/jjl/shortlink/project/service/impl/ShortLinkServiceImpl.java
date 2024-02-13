package com.jjl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.project.common.convention.exception.ServiceException;
import com.jjl.shortlink.project.common.enums.VailDateTypeEnum;
import com.jjl.shortlink.project.dao.entity.*;
import com.jjl.shortlink.project.dao.mapper.*;
import com.jjl.shortlink.project.dto.req.ShortLInkUpdateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.jjl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.jjl.shortlink.project.service.ShortLinkService;
import com.jjl.shortlink.project.utils.HashUtil;
import com.jjl.shortlink.project.utils.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.jjl.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.jjl.shortlink.project.common.constant.ShortLinkConstant.AMAP_API_URL;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapperMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    @Value("${spring.short-link.stats.locale.amap-key}")
    private String amapKey;
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
                throw new ServiceException("短链接已存在");
        }
        stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO, shortLinkDO.getFullShortUrl()), shortLinkCreateReqDTO.getOriginUrl(), LinkUtil.getLinkCacheValidDate(shortLinkCreateReqDTO.getValidDate()), TimeUnit.SECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(shortLinkCreateReqDTO.getDomain() + "/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
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
        String fullShortUrl = serverName + ":" + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            response.sendRedirect(originalLink);
            shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, null, request, response);
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
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
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

    public void shortLinkStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
        try {
            AtomicBoolean uvFirstFlag = new AtomicBoolean(false);
            Cookie[] cookies = request.getCookies();
            Runnable addUv = () -> {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                Cookie cookie = new Cookie("short_uv", uuid);
                cookie.setSecure(false);
                cookie.setMaxAge(60 * 60 * 24 * 30);
                cookie.setPath("/");
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uuid);
                uvFirstFlag.set(Boolean.TRUE);
                ((HttpServletResponse) response).addCookie(cookie);
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies).filter(cookie -> StrUtil.equals(cookie.getName(), "short_uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(cookie -> {
                                    Long add = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, cookie);
                                    uvFirstFlag.set(ObjectUtil.isNotEmpty(add) && add > 0L);
                                },addUv
                        );
            }else {
                addUv.run();
            }
            String remoteAddr = request.getRemoteAddr();
            Long uipAdd = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFlag = ObjectUtil.isNotEmpty(uipAdd) && uipAdd > 0;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLInkGotoDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLInkGotoDO.class)
                        .eq(ShortLInkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLInkGotoDO shortLInkGotoDO = shortLinkGotoMapper.selectOne(lambdaQueryWrapper);
                gid = shortLInkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getValue();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFlag?1:0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapperMapper.shortLinkStats(linkAccessStatsDO);
            Map<String,Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key",amapKey);
            localeParamMap.put("ip",remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_API_URL, localeParamMap);
            JSONObject localeResult = JSON.parseObject(localeResultStr);
            String infoCode =  localeResult.getString("infocode");
            String province = localeResult.getString("province");
            String city = localeResult.getString("city");
            if(StrUtil.isNotBlank(infoCode)&& StrUtil.equals(infoCode,"10000")){
                Boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO localeStatsDO = LinkLocaleStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : city)
                        .adcode(unknownFlag ? "未知" : localeResult.getString("adcode"))
                        .country("中国")
                        .cnt(1)
                        .date(new Date())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(localeStatsDO);

            }
            String os = LinkUtil.getOs(request);
            LinkOsStatsDO osStatsDO = LinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .os(os)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkOsStatsMapper.shortLinkOsState(osStatsDO);
            String browser = LinkUtil.getBrowser(request);
            LinkBrowserStatsDO browserStatsDO = LinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .browser(browser)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(browserStatsDO);
        } catch (Exception e) {
            log.error("短链接访问量统计异常", e);
        }

    }
}
