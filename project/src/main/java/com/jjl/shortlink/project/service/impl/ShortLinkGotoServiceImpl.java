package com.jjl.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjl.shortlink.project.dao.entity.ShortLInkGotoDO;
import com.jjl.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.jjl.shortlink.project.service.ShortLinkGotoService;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkGotoServiceImpl extends ServiceImpl<ShortLinkGotoMapper, ShortLInkGotoDO> implements ShortLinkGotoService {
}
