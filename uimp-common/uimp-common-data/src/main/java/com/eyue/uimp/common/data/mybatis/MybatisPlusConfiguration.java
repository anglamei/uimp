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

package com.eyue.uimp.common.data.mybatis;

import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.eyue.uimp.common.data.datascope.DataScopeHandle;
import com.eyue.uimp.common.data.datascope.DataScopeInterceptor;
import com.eyue.uimp.common.data.datascope.UimpDefaultDatascopeHandle;
import com.eyue.uimp.common.data.tenant.UimpTenantHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author uimp
 * @date 2020-02-08
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisPlusConfiguration {

	/**
	 * 创建租户维护处理器对象
	 *
	 * @return 处理后的租户维护处理器
	 */
	@Bean
	@ConditionalOnMissingBean
	public UimpTenantHandler uimpTenantHandler() {
		return new UimpTenantHandler();
	}

	/**
	 * 分页插件
	 *
	 * @param tenantHandler 租户处理器
	 * @return PaginationInterceptor
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "mybatisPlus.tenantEnable", havingValue = "true", matchIfMissing = true)
	public PaginationInterceptor paginationInterceptor(UimpTenantHandler tenantHandler) {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		List<ISqlParser> sqlParserList = new ArrayList<>();
		TenantSqlParser tenantSqlParser = new TenantSqlParser();
		tenantSqlParser.setTenantHandler(tenantHandler);
		sqlParserList.add(tenantSqlParser);
		paginationInterceptor.setSqlParserList(sqlParserList);
		return paginationInterceptor;
	}

	/**
	 * uimp 默认数据权限处理
	 *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public DataScopeHandle dataScopeHandle() {
		return new UimpDefaultDatascopeHandle();
	}

	/**
	 * 数据权限插件
	 *
	 * @return DataScopeInterceptor
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(DataScopeHandle.class)
	public DataScopeInterceptor dataScopeInterceptor() {
		return new DataScopeInterceptor(dataScopeHandle());
	}
}
