package com.clinbrain.dip.configuration;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EhcacheConfig {

	@Bean
	@Primary
	public CacheManager ehcacheManager() {
		log.info("加载ehcache...");
		return new EhCacheCacheManager(new net.sf.ehcache.CacheManager());
	}
}
