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
package com.eyue.uimp.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eyue.uimp.admin.api.dto.MenuTree;
import com.eyue.uimp.admin.api.entity.*;
import com.eyue.uimp.admin.api.vo.TreeUtil;
import com.eyue.uimp.admin.mapper.SysTenantMapper;
import com.eyue.uimp.admin.service.*;
import com.eyue.uimp.common.core.constant.CacheConstants;
import com.eyue.uimp.common.core.constant.CommonConstants;
import com.eyue.uimp.common.core.exception.CheckedException;
import com.eyue.uimp.common.data.tenant.TenantContextHolder;
import lombok.AllArgsConstructor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ??????
 *
 * @author uimp
 * @date 2019-05-15 15:55:41
 */
@Service
@AllArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {
	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
	private final SysOauthClientDetailsService clientServices;
	private final SysDeptRelationService deptRelationService;
	private final SysUserRoleService userRoleService;
	private final SysRoleMenuService roleMenuService;
	private final SysDictItemService dictItemService;
	private final SysUserService userService;
	private final SysRoleService roleService;
	private final SysMenuService menuService;
	private final SysDeptService deptService;
	private final SysDictService dictService;

	/**
	 * ????????????????????????
	 * <p>
	 * 1. ????????????
	 * 2. ????????????????????????????????????
	 * 3. ????????????????????????????????????
	 *
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstants.TENANT_DETAILS)
	public List<SysTenant> getNormalTenant() {
		return baseMapper.selectList(Wrappers.<SysTenant>lambdaQuery()
				.eq(SysTenant::getStatus, CommonConstants.STATUS_NORMAL));
	}

	/**
	 * ????????????
	 * <p>
	 * 1. ????????????
	 * 2. ????????????????????????
	 * - sys_user
	 * - sys_role
	 * - sys_menu
	 * - sys_user_role
	 * - sys_role_menu
	 * - sys_dict
	 * - sys_dict_item
	 * - sys_client_details
	 *
	 * @param sysTenant ????????????
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.TENANT_DETAILS)
	public Boolean saveTenant(SysTenant sysTenant) {
		this.save(sysTenant);

		// ????????????????????????
		List<SysDict> dictList = new ArrayList<>(dictService.list());
		// ??????????????????????????????
		List<Integer> dictIdList = dictList.stream().map(SysDict::getId)
				.collect(Collectors.toList());
		List<SysDictItem> dictItemList = new ArrayList<>(dictItemService
				.list(Wrappers.<SysDictItem>lambdaQuery()
						.in(SysDictItem::getDictId, dictIdList)));
		// ????????????????????????
		List<SysMenu> menuList = menuService.list();
		// ?????????????????????
		List<SysOauthClientDetails> clientDetailsList = clientServices.list();
		// ?????????????????????????????????
		TenantContextHolder.setTenantId(sysTenant.getId());
		Configuration config = getConfig();

		// ????????????
		SysDept dept = new SysDept();
		dept.setName(config.getString("defaultDeptName"));
		dept.setParentId(0);
		deptService.save(dept);
		//??????????????????
		deptRelationService.insertDeptRelation(dept);
		// ?????????????????????
		SysUser user = new SysUser();
		user.setUsername(config.getString("defaultUsername"));
		user.setPassword(ENCODER.encode(config.getString("defaultPassword")));
		user.setDeptId(dept.getDeptId());
		userService.save(user);
		// ???????????????
		SysRole role = new SysRole();
		role.setRoleCode(config.getString("defaultRoleCode"));
		role.setRoleName(config.getString("defaultRoleName"));
		roleService.save(role);
		// ??????????????????
		SysUserRole userRole = new SysUserRole();
		userRole.setUserId(user.getUserId());
		userRole.setRoleId(role.getRoleId());
		userRoleService.save(userRole);
		// ??????????????????
		saveTenantMenu(TreeUtil.buildTree(menuList, CommonConstants.MENU_TREE_ROOT_ID), CommonConstants.MENU_TREE_ROOT_ID);
		List<SysMenu> newMenuList = menuService.list();

		// ??????????????????,????????????????????????
		List<SysRoleMenu> collect = newMenuList.stream().map(menu -> {
			SysRoleMenu roleMenu = new SysRoleMenu();
			roleMenu.setRoleId(role.getRoleId());
			roleMenu.setMenuId(menu.getMenuId());
			return roleMenu;
		}).collect(Collectors.toList());
		roleMenuService.saveBatch(collect);
		// ??????????????????
		dictService.saveBatch(dictList);
		// ????????????????????????????????????ID
		List<SysDictItem> itemList = dictList.stream()
				.flatMap(dict -> dictItemList.stream()
						.filter(item -> item.getType().equals(dict.getType()))
						.peek(item -> item.setDictId(dict.getId())))
				.collect(Collectors.toList());

		//???????????????
		clientServices.saveBatch(clientDetailsList);
		return dictItemService.saveBatch(itemList);
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @param nodeList ?????????
	 * @param parent   ??????
	 */
	private void saveTenantMenu(List<MenuTree> nodeList, Integer parent) {
		for (MenuTree node : nodeList) {
			SysMenu menu = new SysMenu();
			BeanUtils.copyProperties(node, menu, "parentId");
			menu.setParentId(parent);
			menuService.save(menu);
			if (CollUtil.isNotEmpty(node.getChildren())) {
				List<MenuTree> childrenList = node.getChildren().stream()
						.map(treeNode -> (MenuTree) treeNode).collect(Collectors.toList());
				saveTenantMenu(childrenList, menu.getMenuId());
			}
		}
	}


	/**
	 * ??????????????????
	 */
	private Configuration getConfig() {
		try {
			return new PropertiesConfiguration("tenant/tenant.properties");
		} catch (ConfigurationException e) {
			throw new CheckedException("???????????????????????????", e);
		}
	}
}
