package com.pig4cloud.pig.auth.sso;

/**
 * @ClassName SSOCommonConfig
 * @Author Duys
 * @Description
 * @Date 2022/7/26 9:33
 **/
/*@Configuration
public class SSOCommonConfig {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Bean
	@Qualifier(value = "ssoTokenStore")
	public TokenStore ssoTokenStore() {
		RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
		tokenStore.setPrefix(CacheConstants.PROJECT_SSO_OAUTH_ACCESS);
		return tokenStore;
	}
}*/
