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
package com.eyue.uimp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eyue.uimp.admin.api.entity.SysDict;
import com.eyue.uimp.common.core.util.R;

/**
 * 字典表
 *
 * @author uimp
 * @date 2019/03/19
 */
public interface SysDictService extends IService<SysDict> {

	/**
	 * 根据ID 删除字典
	 *
	 * @param id
	 * @return
	 */
	R removeDict(Integer id);

	/**
	 * 更新字典
	 *
	 * @param sysDict 字典
	 * @return
	 */
	R updateDict(SysDict sysDict);
}
