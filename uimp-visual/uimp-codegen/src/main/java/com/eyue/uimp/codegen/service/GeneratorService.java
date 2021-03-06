/*
 *    Copyright (c) 2018-2025, uimp All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: uimp
 */

package com.eyue.uimp.codegen.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eyue.uimp.codegen.entity.GenConfig;

import java.util.List;
import java.util.Map;

/**
 * @author uimp
 * @date 2018/7/29
 */
public interface GeneratorService {
		/**
		 * 生成代码
		 *
		 * @param tableNames 表名称
		 * @return
		 */
		byte[] generatorCode(GenConfig tableNames);

		/**
		 * 分页查询表
		 *
		 * @param page      分页信息
		 * @param tableName 表名
		 * @param name        数据源ID
		 * @return
		 */
		IPage<List<Map<String, Object>>> getPage(Page page, String tableName, String name);
}
