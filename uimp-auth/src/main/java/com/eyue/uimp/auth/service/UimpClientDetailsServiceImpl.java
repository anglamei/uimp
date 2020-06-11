package com.eyue.uimp.auth.service;

import com.eyue.uimp.common.core.constant.CacheConstants;
import com.eyue.uimp.common.core.constant.SecurityConstants;
import com.eyue.uimp.common.data.tenant.TenantContextHolder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * @author uimp
 * @date 2020/03/25
 * <p>
 * 扩展 JdbcClientDetailsService 支持多租户
 */
@Service
public class UimpClientDetailsServiceImpl extends JdbcClientDetailsService {
	public UimpClientDetailsServiceImpl(DataSource dataSource) {
		super(dataSource);
	}


	/**
	 * 重写原生方法支持redis缓存
	 *
	 * @param clientId
	 * @return ClientDetails
	 * @throws InvalidClientException
	 */
	@Override
	@Cacheable(value = CacheConstants.CLIENT_DETAILS_KEY, key = "#clientId", unless = "#result == null")
	public ClientDetails loadClientByClientId(String clientId) {
		super.setSelectClientDetailsSql(String.format(SecurityConstants.DEFAULT_SELECT_STATEMENT
				, TenantContextHolder.getTenantId()));
		return super.loadClientByClientId(clientId);
	}
}
