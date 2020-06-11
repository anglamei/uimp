package com.eyue.uimp.monitor.controller;

import com.eyue.uimp.common.core.util.R;
import com.eyue.uimp.monitor.service.RedisService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author uimp
 * @date 2019-05-08
 * <p>
 * redis 数据
 */
@RestController
@AllArgsConstructor
@RequestMapping("/redis")
@Api(value = "/redis", tags = "获取redis信息")
public class RedisController {
	private final RedisService redisService;

	/**
	 * 查询redis信息
	 *
	 * @return
	 */
	@GetMapping("/info")
	public R memory() {
		return R.ok(redisService.getInfo());
	}
}
