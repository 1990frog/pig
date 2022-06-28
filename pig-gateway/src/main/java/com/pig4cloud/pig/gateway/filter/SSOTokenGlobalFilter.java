/*
 *
 *  *  Copyright (c) 2019-2020, 冷冷 (wangiegie@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.pig4cloud.pig.gateway.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.gateway.sso.CustomAutoLogin;
import com.pig4cloud.pig.gateway.sso.SSOClientInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lengleng
 * @date 2019/2/1
 * <p>
 * 全局拦截器，作用所有的微服务
 * <p>
 * 1. 连接sso登录逻辑： 前端访问后台接口，header中带 token 参数（从sso平台登录后获取）
 * 过滤器通过token.去sso平台获取用户信息来验证token失效与否，不失效则模拟登录本平台，失效则清除信息。返回401错误
 * <p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SSOTokenGlobalFilter implements GlobalFilter, Ordered {

	private static final String SSO_TOKEN_CACHE = "sso:cache:token:";

	private final CustomAutoLogin autoLogin;

	private final SSOClientInfo ssoClientInfo;

	private final RestTemplate restTemplate;

	private final CacheManager cacheManager;


	private final ObjectMapper objectMapper;

	/**
	 * Process the Web request and (optionally) delegate to the next {@code WebFilter}
	 * through the given {@link GatewayFilterChain}.
	 *
	 * @param exchange the current server exchange
	 * @param chain    provides a way to delegate to the next filter
	 * @return {@code Mono<Void>} to indicate when request processing is complete
	 */
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
		if (StrUtil.isEmpty(token)) {
			// 可能从header传入，也可能是queryString
			token = request.getHeaders().containsKey("tk") ? request.getHeaders().getFirst("tk") : null;
			log.info("sso 获取token 参数 <tk>  开始，{}", token);
			if (StrUtil.isEmpty(token)) {
				MultiValueMap<String, String> queryParams = request.getQueryParams();
				token = (queryParams == null || queryParams.isEmpty()) ? null : queryParams.getFirst("tk");
			}
		}
		log.info("sso 登录流程 开始");
		log.info("sso 登录流程 开始 token = {}", token);
		log.info("sso 登录流程 开始 sysClass = {}", sysClass);
		String errMsg = "无法验证token，请重新登录！";
		if (!StrUtil.isEmpty(token)) {
			Map<String, String> appNameMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[0]));
			Map<String, String> appCodeMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[1]));
			final Map userInfo = getUser(token, appNameMap.get(sysClass), appCodeMap.get(sysClass));
			Object userName;
			if (userInfo != null && (userName = userInfo.get("Identity")) != null) {
				// 这儿使用用户的真实userCode 和 appName
				// 获取一下拿到的真实用户信息
				// cache ssoClientInfo
				cacheSsoClientInfo();
				cacheServerToken((String) userName, sysClass, token);
				//final Map loginMap = autoLogin.login(String.valueOf(ssoClientInfo.getDefaultUserCode()), ssoClientInfo.getCryptogram(), token,sysClass);
				// appName 和 appCode 我也需要cache，后续要使用
				final Map loginMap = autoLogin.login((String) userName, ssoClientInfo.getCryptogram(),
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
				autoLogin.logout(request);
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

	private Map getUser(String token, String appName, String appCode) {
		final Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		if (cache != null && cache.get(token) != null) {
			return (Map) cache.get(token).get();
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", token);
		//header = Base64(AppName)|TimeStamp|Sign
		//Sign= MD5(Base64(AppName)|AppCode|TimeStamp|Token)
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = Base64.encode(textByte);

		//初始化LocalDateTime对象
		ZoneOffset zoneOffset = ZoneOffset.ofHours(0);
		//初始化LocalDateTime对象
		LocalDateTime localDateTime = LocalDateTime.now();
		long TimeStamp = localDateTime.toEpochSecond(zoneOffset);
		String buffer = base64AppName + "|" + appCode + "|" + TimeStamp + "|" + token;
		String Sign = SecureUtil.md5(buffer);
		String header = base64AppName + "|" + TimeStamp + "|" + Sign;

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + header);
		final HttpEntity<String> entity = new HttpEntity<String>(headers);
		final Map map = restTemplate.exchange(ssoClientInfo.getGetUserInfo() + "?token=" + token,
				HttpMethod.GET, entity, Map.class).getBody();
		if (map != null && !map.keySet().isEmpty()
				&& StrUtil.isNotBlank(Optional.ofNullable(map.get("Identity")).orElse("").toString())) {
			cache.put(token, map);
		}
		return map;
	}

	/**
	 * 还需要把ssoClientInfo cache住，因为后面在用户模块中需要用到
	 */
	private void cacheSsoClientInfo() {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		if (ssoClientInfoCache == null || ssoClientInfoCache.get(CacheConstants.SSO_CLIENT_INFO) == null ||
				ssoClientInfoCache.get(CacheConstants.SSO_CLIENT_INFO).get() == null) {
			Map map = JSONObject.parseObject(JSONObject.toJSONString(this.ssoClientInfo), Map.class);
			ssoClientInfoCache.put(CacheConstants.SSO_CLIENT_INFO, map);
			/*ObjectMapper objectMapper = new ObjectMapper();
			try {
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

	private void cacheServerToken(String userName, String sysClass, String serverToken) {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN);
		ssoClientInfoCache.put(userName + "@@" + sysClass, serverToken);
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

	@Override
	public int getOrder() {
		return -1;
	}

}
