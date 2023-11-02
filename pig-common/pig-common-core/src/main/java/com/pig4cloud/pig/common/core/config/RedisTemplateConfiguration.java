/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.common.core.config;

import com.pig4cloud.pig.common.core.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lengleng
 * @date 2019/2/1 Redis 配置类
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisTemplateConfiguration {

	private final RedisConnectionFactory factory;

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
		redisTemplate.setConnectionFactory(factory);

		return redisTemplate;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		return new RedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ZERO),
				this.getRedisCacheConfigurationMap());

	}

	private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
		Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
		//SsoCache和BasicDataCache进行过期时间配置
		redisCacheConfigurationMap.put(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_SERVER_LOCAL_TOKEN,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_LOCAL_SERVER_TOKEN,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_SERVER_INFO,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_USER_SERVER_TOKEN,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_LOCAL_USER_INFO_CACHE,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_USER_ROLE_INFO,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		redisCacheConfigurationMap.put(CacheConstants.SSO_USER_PRI_INFO,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(120)));
		/*redisCacheConfigurationMap.put(CacheConstants.SSO_CLIENT_INFO,
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(60)));*/
		return redisCacheConfigurationMap;
	}

	@Bean
	public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForHash();
	}

	@Bean
	public ValueOperations<String, String> valueOperations(RedisTemplate<String, String> redisTemplate) {
		return redisTemplate.opsForValue();
	}

	@Bean
	public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForList();
	}

	@Bean
	public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForSet();
	}

	@Bean
	public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForZSet();
	}

}
