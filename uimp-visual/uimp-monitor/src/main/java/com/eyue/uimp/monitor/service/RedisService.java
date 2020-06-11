package com.eyue.uimp.monitor.service;

import java.util.Map;

/**
 * @author uimp
 * @date 2019-05-08
 * <p>
 * redis 监控
 */
public interface RedisService {
	/**
	 * 获取内存信息
	 *
	 * @return
	 */
	Map<String, Object> getInfo();
}
