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

package com.pig4cloud.pig.auth.endpoint;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.CommonConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.Inner;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lengleng
 * @date 2019/2/1 删除token端点
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/token")
public class PigTokenEndpoint {

	private final ClientDetailsService clientDetailsService;

	private final TokenStore tokenStore;

	private final RedisTemplate redisTemplate;

	private final CacheManager cacheManager;

	/**
	 * 认证页面
	 *
	 * @param modelAndView
	 * @param error        表单登录失败处理回调的错误信息
	 * @return ModelAndView
	 */
	@GetMapping("/login")
	public ModelAndView require(ModelAndView modelAndView, @RequestParam(required = false) String error) {
		modelAndView.setViewName("ftl/login");
		modelAndView.addObject("error", error);
		return modelAndView;
	}

	/**
	 * 确认授权页面
	 *
	 * @param request
	 * @param session
	 * @param modelAndView
	 * @return
	 */
	@GetMapping("/confirm_access")
	public ModelAndView confirm(HttpServletRequest request, HttpSession session, ModelAndView modelAndView) {
		Map<String, Object> scopeList = (Map<String, Object>) request.getAttribute("scopes");
		modelAndView.addObject("scopeList", scopeList.keySet());

		Object auth = session.getAttribute("authorizationRequest");
		if (auth != null) {
			AuthorizationRequest authorizationRequest = (AuthorizationRequest) auth;
			ClientDetails clientDetails = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());
			modelAndView.addObject("app", clientDetails.getAdditionalInformation());
			modelAndView.addObject("user", SecurityUtils.getUser());
		}

		modelAndView.setViewName("ftl/confirm");
		return modelAndView;
	}

	/**
	 * 退出并删除token
	 *
	 * @param authHeader Authorization
	 */
	@DeleteMapping("/logout")
	public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		if (StrUtil.isBlank(authHeader)) {
			return R.ok();
		}

		String tokenValue = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
		return removeToken(tokenValue);
	}

	/**
	 * 令牌管理调用
	 *
	 * @param token token
	 */
	@Inner
	@DeleteMapping("/{token}")
	public R<Boolean> removeToken(@PathVariable("token") String token) {
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
		if (accessToken == null || StrUtil.isBlank(accessToken.getValue())) {
			return R.ok();
		}

		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(accessToken);
		// 清空用户信息
		cacheManager.getCache(CacheConstants.USER_DETAILS).evict(auth2Authentication.getName());

		// 清空access token
		tokenStore.removeAccessToken(accessToken);

		// 清空 refresh token
		OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
		tokenStore.removeRefreshToken(refreshToken);
		// 清理现场
		clearSSOInfo(token, auth2Authentication);
		return R.ok();
	}

	/**
	 * 查询token
	 *
	 * @param params 分页参数
	 * @return
	 */
	@Inner
	@PostMapping("/page")
	public R<Page> tokenList(@RequestBody Map<String, Object> params) {
		// 根据分页参数获取对应数据
		String key = String.format("%sauth_to_access:*", CacheConstants.PROJECT_OAUTH_ACCESS);
		List<String> pages = findKeysForPage(key, MapUtil.getInt(params, CommonConstants.CURRENT),
				MapUtil.getInt(params, CommonConstants.SIZE));

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		Page result = new Page(MapUtil.getInt(params, CommonConstants.CURRENT),
				MapUtil.getInt(params, CommonConstants.SIZE));
		result.setRecords(redisTemplate.opsForValue().multiGet(pages));
		result.setTotal(redisTemplate.keys(key).size());
		return R.ok(result);
	}

	private List<String> findKeysForPage(String patternKey, int pageNum, int pageSize) {
		ScanOptions options = ScanOptions.scanOptions().count(1000L).match(patternKey).build();
		RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
		Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(
				redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
		List<String> result = new ArrayList<>();
		int tmpIndex = 0;
		int startIndex = (pageNum - 1) * pageSize;
		int end = pageNum * pageSize;

		assert cursor != null;
		while (cursor.hasNext()) {
			if (tmpIndex >= startIndex && tmpIndex < end) {
				result.add(cursor.next().toString());
				tmpIndex++;
				continue;
			}
			if (tmpIndex >= end) {
				break;
			}
			tmpIndex++;
			cursor.next();
		}

		try {
			cursor.close();
		} catch (IOException e) {
			log.error("关闭cursor 失败");
		}
		return result;
	}

	private void clearSSOInfo(String localToken, OAuth2Authentication auth2Authentication) {
		if (auth2Authentication == null || auth2Authentication.getUserAuthentication() == null) {
			return;
		}
		PigUser pigUser = (PigUser) auth2Authentication.getUserAuthentication().getPrincipal();
		if (pigUser == null) {
			return;
		}
		String key = "@@" + pigUser.getSysClass();
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		if (Objects.isNull(serverTokenCache) || Objects.isNull(serverTokenCache.get(localToken + key))
				|| Objects.isNull(serverTokenCache.get(localToken).get())) {
			return;
		}
		String serverToken = (String) serverTokenCache.get(localToken + key).get();
		if (StrUtil.isEmpty(serverToken)) {
			return;
		}
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		if (cache != null && cache.get(serverToken + key) != null) {
			cache.evict(serverToken + key);
		}
		// SSO_LOCAL_USER_INFO_CACHE
		Cache cache1 = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (cache1 != null && cache1.get(pigUser.getUserCode() + key) != null) {
			cache1.evict(pigUser.getUserCode() + key);
		}
		// SSO_USER_ROLE_INFO = "sso_user_role_info:usercode";
		Cache cache2 = cacheManager.getCache(CacheConstants.SSO_USER_ROLE_INFO);
		if (cache2 != null && cache2.get(localToken) != null) {
			cache2.evict(localToken);
		}
		// SSO_USER_PRI_INFO = "sso_user_pri_info:usercode";
		Cache cache3 = cacheManager.getCache(CacheConstants.SSO_USER_PRI_INFO);
		if (cache3 != null && cache3.get(pigUser.getUserCode() + key) != null) {
			cache3.evict(pigUser.getUserCode() + key);
		}
	}

}
