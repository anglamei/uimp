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

package com.eyue.uimp.common.minio;

import com.eyue.uimp.common.minio.http.MinioEndpoint;
import com.eyue.uimp.common.minio.service.MinioTemplate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * minio 自动配置类
 *
 * @author uimp
 */
@AllArgsConstructor
@EnableConfigurationProperties({MinioProperties.class})
public class MinioAutoConfiguration {
	private final MinioProperties properties;

	@Bean
	@ConditionalOnMissingBean(MinioTemplate.class)
	@ConditionalOnProperty(name = "minio.url")
	MinioTemplate template() {
		return new MinioTemplate(
				properties.getUrl(),
				properties.getAccessKey(),
				properties.getSecretKey()
		);
	}


	@Bean
	@ConditionalOnProperty(name = "minio.endpoint.enable", havingValue = "true")
	public MinioEndpoint minioEndpoint(MinioTemplate template) {
		return new MinioEndpoint(template);
	}

}
