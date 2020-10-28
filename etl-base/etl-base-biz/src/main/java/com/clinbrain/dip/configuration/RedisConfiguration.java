package com.clinbrain.dip.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Created by Liaopan on 2020/8/18 0018.
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfiguration extends CachingConfigurerSupport {

	@Bean("redis")
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofDays(15)).disableCachingNullValues();

		RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(config).build();

		log.info("自定义RedisCacheManager加载完成");

		return redisCacheManager;

	}
}