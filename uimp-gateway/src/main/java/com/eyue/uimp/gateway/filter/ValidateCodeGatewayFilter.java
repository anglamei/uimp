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

package com.eyue.uimp.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eyue.uimp.common.core.constant.CacheConstants;
import com.eyue.uimp.common.core.constant.SecurityConstants;
import com.eyue.uimp.common.core.constant.enums.LoginTypeEnum;
import com.eyue.uimp.common.core.exception.ValidateCodeException;
import com.eyue.uimp.common.core.util.R;
import com.eyue.uimp.common.core.util.WebUtils;
import com.eyue.uimp.gateway.config.FilterIgnorePropertiesConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author uimp
 * @date 2018/7/4
 * ???????????????
 */
@Slf4j
@Component
@AllArgsConstructor
public class ValidateCodeGatewayFilter extends AbstractGatewayFilterFactory {
	private final ObjectMapper objectMapper;
	private final RedisTemplate redisTemplate;
	private final FilterIgnorePropertiesConfig filterIgnorePropertiesConfig;

	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// ???????????????????????????????????????
			if (!StrUtil.containsAnyIgnoreCase(request.getURI().getPath()
					, SecurityConstants.OAUTH_TOKEN_URL, SecurityConstants.SMS_TOKEN_URL
					, SecurityConstants.SOCIAL_TOKEN_URL)) {
				return chain.filter(exchange);
			}

			// ??????token?????????????????????
			String grantType = request.getQueryParams().getFirst("grant_type");
			if (StrUtil.equals(SecurityConstants.REFRESH_TOKEN, grantType)) {
				return chain.filter(exchange);
			}

			// ???????????????????????? ??????????????????
			try {
				String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
				String clientId = WebUtils.getClientId(header);
				if (filterIgnorePropertiesConfig.getClients().contains(clientId)) {
					return chain.filter(exchange);
				}

				// ??????????????????????????????????????????SMS
				if (StrUtil.containsAnyIgnoreCase(request.getURI().getPath(), SecurityConstants.SOCIAL_TOKEN_URL)) {
					String mobile = request.getQueryParams().getFirst("mobile");
					if (StrUtil.containsAny(mobile, LoginTypeEnum.SMS.getType())) {
						throw new ValidateCodeException("??????????????????");
					} else {
						return chain.filter(exchange);
					}
				}

				//???????????????
				checkCode(request);
			} catch (Exception e) {
				ServerHttpResponse response = exchange.getResponse();
				response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
				response.setStatusCode(HttpStatus.PRECONDITION_REQUIRED);
				try {
					return response.writeWith(Mono.just(response.bufferFactory()
							.wrap(objectMapper.writeValueAsBytes(
									R.failed(e.getMessage())))));
				} catch (JsonProcessingException e1) {
					log.error("??????????????????", e1);
				}
			}

			return chain.filter(exchange);
		};
	}

	/**
	 * ??????code
	 *
	 * @param request
	 */
	@SneakyThrows
	private void checkCode(ServerHttpRequest request) {
		String code = request.getQueryParams().getFirst("code");

		if (StrUtil.isBlank(code)) {
			throw new ValidateCodeException("?????????????????????");
		}

		String randomStr = request.getQueryParams().getFirst("randomStr");

		//https://gitee.com/log4j/pig/issues/IWA0D
		String mobile = request.getQueryParams().getFirst("mobile");
		if (StrUtil.isNotBlank(mobile)) {
			randomStr = mobile;
		}

		String key = CacheConstants.DEFAULT_CODE_KEY + randomStr;
		redisTemplate.setKeySerializer(new StringRedisSerializer());

		if (!redisTemplate.hasKey(key)) {
			throw new ValidateCodeException("??????????????????");
		}

		Object codeObj = redisTemplate.opsForValue().get(key);

		if (codeObj == null) {
			throw new ValidateCodeException("??????????????????");
		}

		String saveCode = codeObj.toString();
		if (StrUtil.isBlank(saveCode)) {
			redisTemplate.delete(key);
			throw new ValidateCodeException("??????????????????");
		}

		if (!StrUtil.equals(saveCode, code)) {
			redisTemplate.delete(key);
			throw new ValidateCodeException("??????????????????");
		}

		redisTemplate.delete(key);
	}
}
