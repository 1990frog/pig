package com.pig4cloud.pig.gateway.sso;

import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by Liaopan on 2020-08-25.
 * 自定义登录，访问auth/token登录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAutoLogin {

	private final SSOClientInfo ssoClientInfo;

	private final RestTemplate restTemplate;

	private final CacheManager cacheManager;

	public Map login(String username,String password, String token,String sysClass) {
		String cacheKey = "user:" + token;
		final Cache cache = cacheManager.getCache(CacheConstants.SSO_CLIENT_CACHE);
		if(cache != null && cache.get(cacheKey) != null) {
			return (Map) cache.get(cacheKey).get();
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("username", username);
		formData.add("password", password);
		formData.add("scope", "server");
		formData.add("grant_type", "password");

		HttpHeaders headers = new HttpHeaders();
		headers.add("sysClass",sysClass);
		headers.set("Authorization", getAuthorizationHeader("test", "test"));
		Map<String, Object> map = postForMap(ssoClientInfo.getOauthTokenUrl(), formData, headers);
		if(map != null && !map.isEmpty()) {
			cache.put(cacheKey, map);
		}
		return map;
	}

	public void logout(ServerHttpRequest request) {
		final String authorization = request.getHeaders().getFirst("Authorization");
		if(authorization != null && authorization.startsWith("Bearer")) {
			final String logoutUrl = ssoClientInfo.getOauthTokenUrl().replaceFirst("/oauth/token", "/token/logout");
			final Map result = restTemplate.exchange(logoutUrl, HttpMethod.DELETE, null, Map.class).getBody();
			log.info("登出成功, {}", result);
		}

	}

	private String getAuthorizationHeader(String clientId, String clientSecret) {

		if(clientId == null || clientSecret == null) {
			log.warn("Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.");
		}

		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}

	private Map<String, Object> postForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers) {
		if (headers.getContentType() == null) {
			//headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setContentType(MediaType.APPLICATION_JSON);
		}
		StringBuilder builder = new StringBuilder("?");
		formData.forEach((key,value)->{
			builder.append(key)
					.append("=")
					.append(value.get(0))
					.append("&");
		});
		String queryPath = path + builder.toString().substring(0,builder.length() - 1);
		return restTemplate.exchange(queryPath, HttpMethod.POST,
				new HttpEntity<MultiValueMap<String, String>>(formData, headers), Map.class).getBody();
	}
}
