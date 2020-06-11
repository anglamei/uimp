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

package com.eyue.uimp.gateway;


import com.eyue.uimp.common.gateway.annotation.EnableUimpDynamicRoute;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author uimp
 * @date 2018年06月21日
 * 网关应用
 */
@EnableUimpDynamicRoute
@SpringCloudApplication
public class UimpGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(UimpGatewayApplication.class, args);
	}
}
