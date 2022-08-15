package com.pig4cloud.pig.admin.sso.config;

import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * @ClassName AutoComponentScanSSOConfiguration
 * @Author Duys
 * @Description
 * @Date 2022/7/22 14:56
 **/
@ComponentScan(basePackages = {"com.pig4cloud.pig.admin.sso"})
public class AutoComponentScanSSOConfiguration {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Bean
	public TokenStore tokenStore() {
		RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
		tokenStore.setPrefix(CacheConstants.PROJECT_OAUTH_ACCESS);
		return tokenStore;
	}
}
