package com.clinbrain.dip.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Created by Liaopan on 2020-08-31.
 */
@EnableCaching
@Configuration
public class EhcacheConfig {

	@Bean
	@Primary
	public CacheManager ehcacheManager() {
		System.out.println("加载ehcache...");
		return new EhCacheCacheManager(new net.sf.ehcache.CacheManager());
	}
}
