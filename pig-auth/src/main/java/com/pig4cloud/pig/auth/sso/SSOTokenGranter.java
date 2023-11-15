package com.pig4cloud.pig.auth.sso;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.auth.config.AuthorizationServerConfig;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class SSOTokenGranter {

	@Autowired
	private AuthorizationServerConfig config;

	@Autowired
	private AuthenticationManager manager;

	//private static final String CLIENT_ID = "test";
	@Autowired
	private CacheManager cacheManager;

	public OAuth2AccessToken initToken(Map<String, String> parameters) {
		log.info("sso 内部登录 换token param ={}", parameters);
		// 这儿做一步缓存
		cacheLocalLoginUserInfo(parameters);
		OAuth2RequestFactory factory = config.getEndpoints().getOAuth2RequestFactory();
		AuthorizationServerTokenServices services = config.getEndpoints().getDefaultAuthorizationServerTokenServices();
		ClientDetails clientDetails = buildClientDetails(parameters);
		//config.getEndpoints().getClientDetailsService().loadClientByClientId(CacheConstants.SSO_CLIENT_ID);
		TokenRequest tokenRequest = createRequest(fillTokenParameters(parameters));
		OAuth2Request storedOAuth2Request = factory.createOAuth2Request(clientDetails, tokenRequest);
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedOAuth2Request, userAuthentication(tokenRequest));
		OAuth2AccessToken accessToken = services.createAccessToken(oAuth2Authentication);
		// 把localToken 和 serverToken 做一个缓存
		cacheServerTokenAndLocalToken(oAuth2Authentication.getUserAuthentication().getPrincipal(), accessToken.getValue(), parameters.get("token"));
		// 处理一下名称
		accessToken.getAdditionalInformation().put("username", parameters.get("username").split("@@")[0]);
		return accessToken;
	}

	/**
	 * 生成token使用的参数，洗掉一些无用的参数，怕有未知的影响
	 *
	 * @param parameters
	 * @return
	 */
	public Map<String, String> fillTokenParameters(Map<String, String> parameters) {
		Map<String, String> tokenParam = new HashMap<>();
		tokenParam.put("username", parameters.get("username"));
		tokenParam.put("userCode", parameters.get("userCode"));
		tokenParam.put("sysClass", parameters.get("sysClass"));
		tokenParam.put("password", parameters.get("password"));
		tokenParam.put("grant_type", parameters.get("grant_type"));
		tokenParam.put("scope", parameters.get("scope"));
		tokenParam.put("timestamp", parameters.get("timestamp"));
		return tokenParam;
	}

	private ClientDetails buildClientDetails(Map<String, String> parameters) {
		BaseClientDetails clientDetails = new BaseClientDetails();
		clientDetails.setClientId(CacheConstants.SSO_CLIENT_ID);
		clientDetails.setClientSecret(parameters.get("appCode") + "@@" + parameters.get("userCode"));
		clientDetails.setScope(Arrays.asList("server"));
		clientDetails.setAuthorizedGrantTypes(Arrays.asList("password", "refresh_token", "authorization_code", "client_credentials"));
		clientDetails.setAccessTokenValiditySeconds(120 * 60);
		clientDetails.setRefreshTokenValiditySeconds(120 * 60);
		return clientDetails;
	}

	/**
	 * localToken 和 serverToken映射
	 */
	private void cacheServerTokenAndLocalToken(Object o, String localToken, String serverToken) {
		if (StringUtils.isEmpty(localToken)) {
			return;
		}
		// OSS服务端需要使用的参数
		Cache loginParam = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		loginParam.put(localToken, o);
		// 缓存token
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_SERVER_LOCAL_TOKEN);
		serverTokenCache.put(localToken, serverToken);
	}

	/**
	 * @param parameters sso服务端需要使用的参数
	 */
	private void cacheLocalLoginUserInfo(Map<String, String> parameters) {
		// 有一个为空。不缓存
		if (CollectionUtils.isEmpty(parameters)) {
			return;
		}
		String serverToken = parameters.get("token");
		// OSS服务端需要使用的参数 todo 开始把sysClass 也带上
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		String key = serverToken + "@@" + parameters.get("sysClass");
		cache.put(key, parameters);
	}

	@SneakyThrows
	private Authentication userAuthentication(TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
		String username = parameters.get("userCode") + "@@" + parameters.get("sysClass");
		String password = parameters.get("password");
		//parameters.remove("password");
		Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
		((AbstractAuthenticationToken) userAuth).setDetails(parameters);
		return this.manager.authenticate(userAuth);
	}

	private TokenRequest createRequest(Map<String, String> parameters) {
		OAuth2RequestFactory factory = config.getEndpoints().getOAuth2RequestFactory();
		ClientDetails clientDetails = buildClientDetails(parameters);
		//config.getEndpoints().getClientDetailsService().loadClientByClientId(CacheConstants.SSO_CLIENT_ID);
		return factory.createTokenRequest(parameters, clientDetails);
	}
}
