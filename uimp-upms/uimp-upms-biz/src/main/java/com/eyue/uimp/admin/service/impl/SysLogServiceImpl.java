/*
 *
 *      Copyright (c) 2018-2025, uimp All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: uimp
 *
 */

package com.eyue.uimp.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eyue.uimp.admin.api.entity.SysLog;
import com.eyue.uimp.admin.api.vo.PreLogVO;
import com.eyue.uimp.admin.mapper.SysLogMapper;
import com.eyue.uimp.admin.service.SysLogService;
import com.eyue.uimp.common.core.constant.CommonConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author uimp
 * @since 2017-11-20
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

	/**
	 * 批量插入前端错误日志
	 *
	 * @param preLogVoList 日志信息
	 * @return true/false
	 */
	@Override
	public Boolean saveBatchLogs(List<PreLogVO> preLogVoList) {
		List<SysLog> sysLogs = preLogVoList.stream()
			.map(pre -> {
				SysLog log = new SysLog();
				log.setType(CommonConstants.STATUS_LOCK);
				log.setTitle(pre.getInfo());
				log.setException(pre.getStack());
				log.setParams(pre.getMessage());
				log.setCreateTime(LocalDateTime.now());
				log.setRequestUri(pre.getUrl());
				log.setCreateBy(pre.getUser());
				return log;
			})
			.collect(Collectors.toList());
		return this.saveBatch(sysLogs);
	}
}
