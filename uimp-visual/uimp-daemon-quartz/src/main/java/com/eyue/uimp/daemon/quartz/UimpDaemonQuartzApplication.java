package com.eyue.uimp.daemon.quartz;

import com.eyue.uimp.common.feign.annotation.EnableUimpFeignClients;
import com.eyue.uimp.common.security.annotation.EnableUimpResourceServer;
import com.eyue.uimp.common.swagger.annotation.EnableUimpSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author frwcloud
 * @date 2019/01/23
 * 定时任务模块
 */
@EnableUimpSwagger2
@EnableUimpFeignClients
@SpringCloudApplication
@EnableUimpResourceServer
public class UimpDaemonQuartzApplication {

	public static void main(String[] args) {
		SpringApplication.run(UimpDaemonQuartzApplication.class, args);
	}
}
