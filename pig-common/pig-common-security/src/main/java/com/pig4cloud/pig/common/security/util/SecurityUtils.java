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

package com.pig4cloud.pig.common.security.util;

import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import lombok.experimental.UtilityClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 安全工具类
 *
 * @author L.cm
 */
@UtilityClass
public class SecurityUtils {

	/**
	 * 获取Authentication
	 */
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 获取用户
	 */
	public PigUser getUser(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof PigUser) {
			return (PigUser) principal;
		}
		return null;
	}

	/**
	 * 获取用户
	 */
	public PigUser getUser() {
		Authentication authentication = getAuthentication();
		if (authentication == null) {
			return null;
		}
		CacheManager cacheManager = SpringContextFactory.getApplicationContext().getBean(CacheManager.class);
		if (enableSSO(cacheManager)) {
			return getUserNew(cacheManager);
		}
		return getUser(authentication);
	}

	public String getSSOToken() {
		String token = LocalTokenHolder.getToken();
		CacheManager cacheManager = SpringContextFactory.getApplicationContext().getBean(CacheManager.class);
		if (enableSSO(cacheManager)) {
			return getSSOToken(token, cacheManager);
		}
		return token;
	}

	private boolean enableSSO(CacheManager cacheManager) {
		if (cacheManager != null) {
			Map map = null;
			Cache cache = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
			if (cache != null && cache.get(CacheConstants.SSO_CLIENT_INFO) != null) {
				map = (Map) cache.get(CacheConstants.SSO_CLIENT_INFO).get();
			}
			if (map != null) {
				Boolean enable = (Boolean) map.get("enable");
				if (enable != null && enable.booleanValue()) {
					return true;
				}
			}
		}
		return false;
	}

	private PigUser getUserNew(CacheManager cacheManager) {
		String token = LocalTokenHolder.getToken();
		Cache cache = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		if (cache != null && cache.get(token) != null) {
			return (PigUser) cache.get(token).get();
		}
		return null;
	}

	private String getSSOToken(String token, CacheManager cacheManager) {
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_SERVER_LOCAL_TOKEN);
		if (serverTokenCache != null || serverTokenCache.get(token) != null) {
			return (String) serverTokenCache.get(token).get();
		}
		return null;
	}

	/**
	 * 获取用户角色信息
	 *
	 * @return 角色集合
	 */
	public List<Integer> getRoles() {
		Authentication authentication = getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		List<Integer> roleIds = new ArrayList<>();
		authorities.stream().filter(granted -> StrUtil.startWith(granted.getAuthority(), SecurityConstants.ROLE))
				.forEach(granted -> {
					String id = StrUtil.removePrefix(granted.getAuthority(), SecurityConstants.ROLE);
					roleIds.add(Integer.parseInt(id));
				});
		return roleIds;
	}

}
