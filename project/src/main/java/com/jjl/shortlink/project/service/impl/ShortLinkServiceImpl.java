package com.jjl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.jjl.shortlink.project.common.convention.exception.ClientException;
import com.jjl.shortlink.project.common.convention.exception.ServiceException;
import com.jjl.shortlink.project.common.enums.VailDateTypeEnum;
import com.jjl.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.jjl.shortlink.project.dao.entity.*;
import com.jjl.shortlink.project.dao.mapper.*;
import com.jjl.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.jjl.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.jjl.shortlink.project.dto.resp.*;
import com.jjl.shortlink.project.service.LinkStatsTodayService;
import com.jjl.shortlink.project.service.ShortLinkService;
import com.jjl.shortlink.project.utils.HashUtil;
import com.jjl.shortlink.project.utils.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.jjl.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.jjl.shortlink.project.common.constant.ShortLinkConstant.AMAP_API_URL;

@Service
@Slf4j
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
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;
    private final LinkStatsTodayService linkStatsTodayService;
    @Value("${spring.short-link.stats.locale.amap-key}")
    private String amapKey;
    @Value("${spring.short-link.domain.default}")
    private String domain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String suffix = generateSuffix(shortLinkCreateReqDTO);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(shortLinkCreateReqDTO, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(domain + "/" + suffix);
        shortLinkDO.setShortUri(suffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setDelFlag(0);
        shortLinkDO.setTotalUv(0);
        shortLinkDO.setTotalPv(0);
        shortLinkDO.setTodayPv(0);
        shortLinkDO.setTodayUv(0);
        shortLinkDO.setTotalUip(0);
        ShortLinkGotoDO shortLInkGotoDO = BeanUtil.toBean(shortLinkDO, ShortLinkGotoDO.class);
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
        IPage<ShortLinkDO> shortLinkDOIPage = baseMapper.pageLink(shortLinkPageReqDTO);
        return shortLinkDOIPage.convert(e -> BeanUtil.toBean(e, ShortLinkPageRespDTO.class));
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
    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            if (!rLock.tryLock()) {
                throw new ServiceException("短链接正在被访问，请稍后再试...");
            }
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(domain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapperMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())) {
            stringRedisTemplate.delete(String.format(SHORT_LINK_GOTO, requestParam.getFullShortUrl()));
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().isBefore(LocalDateTime.now())) {
                if (Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().isAfter(LocalDateTime.now())) {
                    stringRedisTemplate.delete(String.format(SHORT_LINK_GOTO_ISNULL, requestParam.getFullShortUrl()));
                }
            }
        }
        return null;
    }
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                ex.printStackTrace();
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
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
            LambdaQueryWrapper<ShortLinkGotoDO> wrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLInkGotoDO = shortLinkGotoMapper.selectOne(wrapper);
            //短链接不存在
            if (ObjectUtil.isEmpty(shortLInkGotoDO)) {
                stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLInkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus,0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (ObjectUtil.isEmpty(shortLinkDO)) {
                response.sendRedirect("/page/notfound");
                return;
            }
            if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().isBefore(LocalDateTime.now())) {
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
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addUv = () -> {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                uv.set(uuid);
                Cookie cookie = new Cookie("short_uv", uuid);
                cookie.setSecure(false);
                cookie.setMaxAge(60 * 60 * 24 * 30);
                cookie.setPath("/");
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uuid);
                uvFirstFlag.set(Boolean.TRUE);
                response.addCookie(cookie);
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies).filter(cookie -> StrUtil.equals(cookie.getName(), "short_uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(cookie -> {
                                    uv.set(cookie);
                                    Long add = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, cookie);
                                    uvFirstFlag.set(ObjectUtil.isNotEmpty(add) && add > 0L);
                                }, addUv
                        );
            } else {
                addUv.run();
            }
            String remoteAddr = request.getRemoteAddr();
            Long uipAdd = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFlag = ObjectUtil.isNotEmpty(uipAdd) && uipAdd > 0;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLInkGotoDO = shortLinkGotoMapper.selectOne(lambdaQueryWrapper);
                gid = shortLInkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getValue();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapperMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", amapKey);
            localeParamMap.put("ip", remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_API_URL, localeParamMap);
            JSONObject localeResult = JSON.parseObject(localeResultStr);
            String infoCode = localeResult.getString("infocode");
            String province = localeResult.getString("province");
            String city = localeResult.getString("city");
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
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
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .ip(remoteAddr)
                    .os(os)
                    .browser(browser)
                    .network(LinkUtil.getNetwork(request))
                    .device(LinkUtil.getDevice(request))
                    .locale(StrUtil.join("-", "中国", province, city))
                    .user(uv.get())
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(LinkUtil.getDevice(request))
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(LinkUtil.getNetwork(request))
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFlag ? 1 : 0);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .todayPv(1)
                    .todayUv(uvFirstFlag.get() ? 1 : 0)
                    .todayUip(uipFlag ? 1 : 0)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
        } catch (Exception e) {
            log.error("短链接访问量统计异常", e);
        }

    }
}
