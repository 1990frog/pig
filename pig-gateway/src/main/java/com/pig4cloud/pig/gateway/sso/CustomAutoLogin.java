package com.pig4cloud.pig.gateway.sso;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public Map login(String username, String password, String token, String sysClass, String appName, String appCode) {
		//String cacheKey = "user:" + token;
		/*final Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		if (cache != null && cache.get(token) != null) {
			return (Map) cache.get(token).get();
		}*/

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("username", username + "@@" + sysClass);
		parameters.add("password", password);
		parameters.add("grant_type", "password");
		parameters.add("scope", "server");
		parameters.add("token", token);// 把token带过去，做映射
		parameters.add("appCode", appCode);
		parameters.add("appName", appName);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ssoClientInfo.getOauthTokenUrl());
		URI uri = builder.queryParams(parameters).build().encode().toUri();
		ResponseEntity<Map> forEntity = restTemplate.getForEntity(uri, Map.class);
		if (forEntity.getBody() == null) {
			return null;
		}
		return forEntity.getBody();

//		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
//		formData.add("username", username);
//		formData.add("password", password);
//		formData.add("scope", "server");
//		formData.add("grant_type", "password");
//		formData.add("random",String.valueOf(System.currentTimeMillis()));
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("sysClass",sysClass);
//		headers.set("Authorization", getAuthorizationHeader("test", "test"));
//		headers.set("Connection", "Close");
//		Map<String, Object> map = postForMap(ssoClientInfo.getOauthTokenUrl(), formData, headers);
//		if(map != null && !map.isEmpty()) {
//			cache.put(cacheKey, map);
//		}
//		return map;
	}

	public void logout(ServerHttpRequest request) {
		final String authorization = request.getHeaders().getFirst("Authorization");
		if (authorization != null && authorization.startsWith("Bearer")) {
			final String logoutUrl = ssoClientInfo.getOauthTokenUrl().replaceFirst("/oauth/token", "/token/logout");
			final Map result = restTemplate.exchange(logoutUrl, HttpMethod.DELETE, null, Map.class).getBody();
			log.info("登出成功, {}", result);
		}

	}

	private String getAuthorizationHeader(String clientId, String clientSecret) {

		if (clientId == null || clientSecret == null) {
			log.warn("Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.");
		}

		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}

	private Map<String, Object> postForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers) {
		StringBuilder builder = new StringBuilder("?");
		formData.forEach((key, value) -> {
			builder.append(key)
					.append("=")
					.append(value.get(0))
					.append("&");
		});
		String queryPath = path + builder.toString().substring(0, builder.length() - 1);
		return post(queryPath, headers);
//		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//		factory.setConnectTimeout(5000);
//		factory.setReadTimeout(5000);
//		restTemplate.setRequestFactory(factory);
//		return restTemplate.exchange(queryPath, HttpMethod.POST,
//				new HttpEntity<MultiValueMap<String, String>>(null, headers), Map.class).getBody();
	}

	@SneakyThrows
	private Map<String, Object> post(String path, HttpHeaders headers) {
		HttpEntity httpEntity = null;
		HttpPost httpPost = null;
		try {
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setSocketTimeout(5000)
					.setConnectTimeout(5000)
					.setConnectionRequestTimeout(5000)
					.build();
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
			httpPost = new HttpPost(path);
			Set<Map.Entry<String, List<String>>> sets = headers.entrySet();
			Iterator<Map.Entry<String, List<String>>> it = sets.iterator();
			while (it.hasNext()) {
				Map.Entry<String, List<String>> header = it.next();
				httpPost.setHeader(header.getKey(), header.getValue().get(0));
			}
			log.info("[{}]-sso内部登录请求:[{}]", LocalDateTime.now().toString(), JSON.toJSONString(httpPost));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			httpEntity = httpResponse.getEntity();

			String resultString = EntityUtils.toString(httpEntity, "utf-8");
			return JSON.parseObject(resultString, Map.class);
		} finally {
			EntityUtils.consumeQuietly(httpEntity);
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
		}
	}
}
