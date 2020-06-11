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

package com.eyue.uimp.daemon.elastic;

import com.eyue.uimp.common.feign.annotation.EnableUimpFeignClients;
import com.eyue.uimp.common.job.annotation.EnableUimpXxlJob;
import com.eyue.uimp.common.security.annotation.EnableUimpResourceServer;
import com.eyue.uimp.common.swagger.annotation.EnableUimpSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author uimp
 * @date 2018/7/24
 * 分布式任务调度模块
 */
@EnableUimpXxlJob
@EnableUimpSwagger2
@EnableUimpFeignClients
@SpringCloudApplication
@EnableUimpResourceServer
public class UimpDaemonElasticJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(UimpDaemonElasticJobApplication.class, args);
	}

}
