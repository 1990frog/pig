package com.pig4cloud.pig.common.sso.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.sso.component.SSOClientInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName SSOTokenGlobalFilter
 * @Author Duys
 * @Description sso 登录拦截器
 * @Date 2022/7/20 14:33
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class SSOTokenGlobalFilter implements GlobalFilter, Ordered {

	private final SSOClientInfo ssoClientInfo;

	private final RestTemplate restTemplate;

	private final CacheManager cacheManager;

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpRequest request = exchange.getRequest();
		final String authorization = request.getHeaders().getFirst("Authorization");
		if (!ssoClientInfo.isEnable()
				|| StrUtil.startWith(authorization, "Bearer")) {
			return chain.filter(exchange);
		}
		// sso 的token
		String token = request.getHeaders().getFirst("token");
		String sysClass = request.getHeaders().getFirst("sysClass");
		// 兼容华西的sso接入
		if (StrUtil.isEmpty(token) || request.getHeaders().containsKey("tk")) {
			log.info("sso 兼容获取token 参数 <tk>  开始, tk = {}", token);
			token = request.getHeaders().getFirst("tk");
		}
		//  可能从header传入，也可能是queryString
		if (StrUtil.isEmpty(token)) {
			MultiValueMap<String, String> queryParams = request.getQueryParams();
			token = (queryParams == null || queryParams.isEmpty()) ? null : queryParams.getFirst("tk");
		}
		log.info("sso 登录流程 开始 token = {} , sysClass = {} ", token, sysClass);
		String errMsg = "无法验证token，请重新登录！";
		if (!StrUtil.isEmpty(token)) {
			Map<String, String> appNameMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[0]));
			Map<String, String> appCodeMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[1]));
			// 走sso 获取用户信息
			final Map userInfo = getUser(token, appNameMap.get(sysClass), appCodeMap.get(sysClass), sysClass);
			// 是否需要把appId和appCode存储
			cacheAppCodeAndId(appCodeMap, token);
			if (userInfo != null && (userInfo.containsKey("Identity"))) {
				// 这儿使用用户的真实userCode 和 appName
				// 获取一下拿到的真实用户信息
				// cache ssoClientInfo
				cacheSsoClientInfo();
				cacheServerToken((String) userInfo.get("Identity"), sysClass, token);
				//final Map loginMap = autoLogin.login(String.valueOf(ssoClientInfo.getDefaultUserCode()), ssoClientInfo.getCryptogram(), token,sysClass);
				// appName 和 appCode 我也需要cache，后续要使用
				final Map loginMap = login((String) userInfo.get("UserName"), (String) userInfo.get("Identity"), ssoClientInfo.getCryptogram(),
						token, sysClass, appNameMap.get(sysClass), appCodeMap.get(sysClass));
				if (loginMap != null) {
					ServerHttpResponse response = exchange.getResponse();
					byte[] bits = new byte[0];
					try {
						bits = objectMapper.writeValueAsString(loginMap)
								.getBytes(StandardCharsets.UTF_8);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					DataBuffer buffer = response.bufferFactory().wrap(bits);
					response.getHeaders().add("Content-Type", "text/json;charset=UTF-8");
					// 登录成功再缓存serverToken 和 localToken
					return response.writeWith(Mono.just(buffer));
				}
			} else {
				logout(request);
				// 登录失败。返回401错误
				if (userInfo != null) {
					errMsg = String.valueOf(userInfo.getOrDefault("message", errMsg));
				}
				ServerHttpResponse response = exchange.getResponse();
				R<String> result = new R<>();
				result.setStatus(cn.hutool.http.HttpStatus.HTTP_UNAUTHORIZED);
				result.setData(ssoClientInfo.getServerUrl() + appNameMap.get(sysClass));
				result.setMessage(errMsg);

				byte[] bits = new byte[0];
				try {
					bits = objectMapper.writeValueAsString(result)
							.getBytes(StandardCharsets.UTF_8);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				DataBuffer buffer = response.bufferFactory().wrap(bits);
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				response.getHeaders().add("Content-Type", "text/json;charset=UTF-8");

				// clearCache
				clearCache(token);
				return response.writeWith(Mono.just(buffer));
			}
		}
		return chain.filter(exchange);
	}

	// 获取一下appid，后面需要用appid
	private void cacheAppCodeAndId(Map<String, String> appCodeMap, String token) {
		Integer type = ssoClientInfo.getType();
		// 传统的sso不需要，
		if (type == null || type.intValue() == 2) {
			return;
		}
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", token);
			final HttpEntity<String> entity = new HttpEntity<String>(headers);
			Set<String> appCodes = appCodeMap.values().stream().collect(Collectors.toSet());
			String ssoHost = ssoClientInfo.getSsoHost();
			String url = ssoHost != null && ssoHost.startsWith("http") ? ssoHost : "http://" + ssoHost;
			url += (url.endsWith("/") ? "" : "/");
			url += "cm/api/App/all";
			ResponseEntity<List<Map<String, Object>>> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {
			});
			if (exchange == null) {
				return;
			}
			List<Map<String, Object>> body = exchange.getBody();
			if (body == null || body.size() <= 0) {
				return;
			}
			Map<String, Integer> appCodeAndId = new HashMap<>();
			for (Map<String, Object> cur : body) {
				String code = (String) cur.get("Code");
				if (appCodes.contains(code)) {
					appCodeAndId.put(code, (Integer) cur.get("ID"));
				}
			}
			Cache cache = cacheManager.getCache(CacheConstants.SSO_APPCODE_ID);
			cache.put("app", appCodeAndId);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取所有应用失败,info={}", e);
		}

	}

	private Map getUser(String token, String appName, String appCode, String sysClass) {
		// 不仅仅针对token，还带上了sysclass
		final Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		String key = token + "@@" + sysClass;
		if (cache != null && cache.get(key) != null) {
			return (Map) cache.get(key).get();
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", token);
		//header = Base64(AppName)|TimeStamp|Sign
		//Sign= MD5(Base64(AppName)|AppCode|TimeStamp|Token)
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + buildAuthorization(token, appName, appCode));
		final HttpEntity<String> entity = new HttpEntity<String>(headers);
		final Map map = restTemplate.exchange(ssoClientInfo.getGetUserInfo() + "?token=" + token,
				HttpMethod.GET, entity, Map.class).getBody();
		if (map != null && !map.keySet().isEmpty()
				&& StrUtil.isNotBlank(Optional.ofNullable(map.get("Identity")).orElse("").toString())) {
			cache.put(key, map);
		}
		return map;
	}

	private String buildAuthorization(String token, String appName, String appCode) {
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = Base64.encode(textByte);

		//初始化LocalDateTime对象
		ZoneOffset zoneOffset = ZoneOffset.ofHours(0);
		//初始化LocalDateTime对象
		LocalDateTime localDateTime = LocalDateTime.now();
		long TimeStamp = localDateTime.toEpochSecond(zoneOffset);
		String buffer = base64AppName + "|" + appCode + "|" + TimeStamp + "|" + token;
		String Sign = SecureUtil.md5(buffer);
		String basic = base64AppName + "|" + TimeStamp + "|" + Sign;
		log.info("basic= {}", basic);
		return basic;
	}

	/**
	 * 还需要把ssoClientInfo cache住，因为后面在用户模块中需要用到
	 */
	private void cacheSsoClientInfo() {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		if (ssoClientInfoCache == null || ssoClientInfoCache.get(CacheConstants.SSO_CLIENT_INFO) == null ||
				ssoClientInfoCache.get(CacheConstants.SSO_CLIENT_INFO).get() == null) {
			Map map = JSONUtil.toBean(JSONUtil.parseObj(this.ssoClientInfo), Map.class);
			ssoClientInfoCache.put(CacheConstants.SSO_CLIENT_INFO, map);
			/*try {
				String str = objectMapper.writeValueAsString(this.ssoClientInfo);
				Map map = objectMapper.readValue(str, Map.class);
				ssoClientInfoCache.put(CacheConstants.SSO_CLIENT_INFO, map);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				log.error("登录异常,类型转换异常 e={}", e);
				throw new RuntimeException("登录异常,类型转换异常");
			}*/
		}
	}

	private void cacheServerToken(String userCode, String sysClass, String serverToken) {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN);
		ssoClientInfoCache.put(userCode + "@@" + sysClass, serverToken);
	}


	/**
	 * 失败了需要把前面步骤的缓存给清除掉
	 */
	private void clearCache(String serverToken) {
		// 双边映射
		Cache serverToLocal = cacheManager.getCache(CacheConstants.SSO_SERVER_LOCAL_TOKEN);
		if (!Objects.isNull(serverToLocal) && !Objects.isNull(serverToLocal.get(serverToken))) {
			serverToLocal.evictIfPresent(serverToken);
		}
		// OSS服务端需要使用的参数
		Cache serverInfo = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		if (!Objects.isNull(serverInfo) && !Objects.isNull(serverInfo.get(serverToken))) {
			serverInfo.evictIfPresent(serverToken);
		}
	}

	// 走本地登录
	public Map login(String username, String userCode, String password, String token, String sysClass, String appName, String appCode) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("username", username + "@@" + sysClass);
		parameters.add("userCode", userCode);
		parameters.add("sysClass", sysClass);
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
	}

	public void logout(ServerHttpRequest request) {
		final String authorization = request.getHeaders().getFirst("Authorization");
		if (authorization != null && authorization.startsWith("Bearer")) {
			final String logoutUrl = ssoClientInfo.getOauthTokenUrl().replaceFirst("/oauth/token", "/token/logout");
			final Map result = restTemplate.exchange(logoutUrl, HttpMethod.DELETE, null, Map.class).getBody();
			log.info("登出成功, {}", result);
		}

	}

	@Override
	public int getOrder() {
		return -1;
	}
}
