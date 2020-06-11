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

package com.eyue.uimp.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author uimp
 * @since 2017-11-08
 */
@Data
@ApiModel(value = "菜单")
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends Model<SysMenu> {

	private static final long serialVersionUID = 1L;
	/**
	 * 菜单ID
	 */
	@TableId(value = "menu_id", type = IdType.AUTO)
	@ApiModelProperty(value = "菜单id")
	private Integer menuId;
	/**
	 * 菜单名称
	 */
	@NotBlank(message = "菜单名称不能为空")
	@ApiModelProperty(value = "菜单名称")
	private String name;
	/**
	 * 菜单权限标识
	 */
	@ApiModelProperty(value = "菜单权限标识")
	private String permission;
	/**
	 * 父菜单ID
	 */
	@NotNull(message = "菜单父ID不能为空")
	@ApiModelProperty(value = "菜单父id")
	private Integer parentId;
	/**
	 * 图标
	 */
	@ApiModelProperty(value = "菜单图标")
	private String icon;
	/**
	 * 前端路由标识路径，默认和 comment 保持一致
	 * 过期
	 */
	@ApiModelProperty(value = "前端路由标识路径")
	private String path;
	/**
	 * 排序值
	 */
	@ApiModelProperty(value = "排序值")
	private Integer sort;
	/**
	 * 菜单类型 （0菜单 1按钮）
	 */
	@NotNull(message = "菜单类型不能为空")
	@ApiModelProperty(value = "菜单类型,0:菜单 1:按钮")
	private String type;
	/**
	 * 路由缓冲
	 */
	@ApiModelProperty(value = "路由缓冲")
	private String keepAlive;
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	@ApiModelProperty(value = "更新时间")
	private LocalDateTime updateTime;
	/**
	 * 0--正常 1--删除
	 */
	@TableLogic
	@ApiModelProperty(value = "删除标记,1:已删除,0:正常")
	private String delFlag;
}
