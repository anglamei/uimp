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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpUtil;
import com.eyue.uimp.common.core.constant.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author uimp
 * @date 2020/1/8
 * ?????????????????????
 * <p>
 * ?????? ModifyRequestBodyGatewayFilterFactory ??????
 */
@Slf4j
@Component
public class PasswordDecoderFilter extends AbstractGatewayFilterFactory {
	private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

	private static final String PASSWORD = "password";
	private static final String KEY_ALGORITHM = "AES";
	@Value("${security.encode.key:1234567812345678}")
	private String encodeKey;


	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			// ???????????????????????????????????????
			if (!StrUtil.containsAnyIgnoreCase(request.getURI().getPath(), SecurityConstants.OAUTH_TOKEN_URL)) {
				return chain.filter(exchange);
			}

			// ??????token?????????????????????
			String grantType = request.getQueryParams().getFirst("grant_type");
			if (StrUtil.equals(SecurityConstants.REFRESH_TOKEN, grantType)) {
				return chain.filter(exchange);
			}

			Class inClass = String.class;
			Class outClass = String.class;
			ServerRequest serverRequest = ServerRequest.create(exchange,
					messageReaders);

			// ????????????????????????
			Mono<?> modifiedBody = serverRequest.bodyToMono(inClass)
					.flatMap(decryptAES());

			BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
			HttpHeaders headers = new HttpHeaders();
			headers.putAll(exchange.getRequest().getHeaders());
			headers.remove(HttpHeaders.CONTENT_LENGTH);

			headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
					exchange, headers);
			return bodyInserter.insert(outputMessage, new BodyInserterContext())
					.then(Mono.defer(() -> {
						ServerHttpRequest decorator = decorate(exchange, headers,
								outputMessage);
						return chain
								.filter(exchange.mutate().request(decorator).build());
					}));
		};
	}

	/**
	 * ????????????
	 *
	 * @return
	 */
	private Function decryptAES() {
		return s -> {
			// ????????????????????????AES ??????
			AES aes = new AES(Mode.CBC, Padding.NoPadding,
					new SecretKeySpec(encodeKey.getBytes(), KEY_ALGORITHM),
					new IvParameterSpec(encodeKey.getBytes()));

			// ???????????????????????????
			Map<String, String> inParamsMap = HttpUtil.decodeParamMap((String) s, CharsetUtil.CHARSET_UTF_8);
			byte[] result = aes.decrypt(Base64.decode(inParamsMap.get(PASSWORD)
					.getBytes(StandardCharsets.UTF_8)));
			String password = new String(result, StandardCharsets.UTF_8);

			// ???????????????????????????
			inParamsMap.put(PASSWORD, password.trim());
			return Mono.just(HttpUtil.toParams(inParamsMap));
		};
	}

	/**
	 * ????????????
	 *
	 * @return
	 */
	private ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
												CachedBodyOutputMessage outputMessage) {
		return new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public HttpHeaders getHeaders() {
				long contentLength = headers.getContentLength();
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.putAll(super.getHeaders());
				if (contentLength > 0) {
					httpHeaders.setContentLength(contentLength);
				} else {
					httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
				}
				return httpHeaders;
			}

			@Override
			public Flux<DataBuffer> getBody() {
				return outputMessage.getBody();
			}
		};
	}
}
