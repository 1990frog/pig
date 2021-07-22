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

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.gateway.sso.CustomAutoLogin;
import com.pig4cloud.pig.gateway.sso.SSOClientInfo;
import lombok.RequiredArgsConstructor;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import sun.misc.BASE64Encoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
		if(!ssoClientInfo.isEnable()
		|| StrUtil.startWith(authorization, "Bearer")) {
			return chain.filter(exchange);
		}
		// sso 的token
		final String token = request.getHeaders().getFirst("token");
		final String sysClass = request.getHeaders().getFirst("sysClass");
		String errMsg = "无法验证token，请重新登录！";
		if (!StringUtils.isEmpty(token)) {
			Map<String,String> appNameMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[0]));
			Map<String,String> appCodeMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[1]));
			final Map userInfo = getUser(token,appNameMap.get(sysClass),appCodeMap.get(sysClass));
			Object userName ;
			if (userInfo != null && (userName = userInfo.get("Identity")) != null) {
				final Map loginMap = autoLogin.login(String.valueOf(ssoClientInfo.getDefaultUserCode()), ssoClientInfo.getCryptogram(), token,sysClass);
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

					return response.writeWith(Mono.just(buffer));
				}
			}else {
				autoLogin.logout(request);
				// 登录失败。返回401错误
				if(userInfo != null) {
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

				return response.writeWith(Mono.just(buffer));
			}
		}
		return chain.filter(exchange);

	}

	private Map getUser(String token,String appName,String appCode) {
		final Cache cache = cacheManager.getCache(CacheConstants.SSO_CLIENT_CACHE);
		if ( cache != null && cache.get(token) != null) {
			return (Map) cache.get(token).get();
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", token);
		//header = Base64(AppName)|TimeStamp|Sign
		//Sign= MD5(Base64(AppName)|AppCode|TimeStamp|Token)
		BASE64Encoder encoder = new BASE64Encoder();
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = encoder.encode(textByte);

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
		if(map != null && !map.keySet().isEmpty()) {
			cache.put(token, map);
		}
		return map;
	}

	@Override
	public int getOrder() {
		return -1;
	}

}
