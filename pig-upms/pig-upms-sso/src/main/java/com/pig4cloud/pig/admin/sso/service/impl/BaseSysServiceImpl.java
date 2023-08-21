package com.pig4cloud.pig.admin.sso.service.impl;

import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.SnowFlakeUtil;
import com.pig4cloud.pig.admin.sso.service.IRemoteService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.security.service.PigUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Map;
import java.util.Objects;

/**
 * @ClassName BaseSysService
 * @Author Duys
 * @Description
 * @Date 2022/7/26 10:29
 **/

public class BaseSysServiceImpl {
	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected RedisTemplate<String, Object> redisTemplate;

	@Autowired
	protected IRemoteService remoteService;

	@Autowired
	protected TokenStore tokenStore;

	protected SnowFlakeUtil idWorker = new SnowFlakeUtil();


	protected String getServerToken(String localToken) {
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		if (Objects.isNull(serverTokenCache) || Objects.isNull(serverTokenCache.get(localToken))
				|| Objects.isNull(serverTokenCache.get(localToken).get())) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		String serverToken = (String) serverTokenCache.get(localToken).get();
		return serverToken;
	}

	protected Map<String, String> toLocalLogin(String serverToken) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		return (Map<String, String>) cache.get(serverToken).get();
	}

	protected Map toServerLogin(String serverToken) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		if (cache != null && cache.get(serverToken) != null) {
			return (Map) cache.get(serverToken).get();
		}
		return null;
	}

	protected String getServerTokenByUserName(String key) {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN);
		return (String) ssoClientInfoCache.get(key).get();
	}


	protected PigUser getPigUser(String userName, String sysClass) {
		String key = userName + "@@" + sysClass;
		Cache userDetailsCache = cacheManager.getCache(CacheConstants.USER_DETAILS);
		if (!Objects.isNull(userDetailsCache) && !Objects.isNull(userDetailsCache.get(key))
				&& !Objects.isNull(userDetailsCache.get(key))) {
			return (PigUser) userDetailsCache.get(key).get();
		}
		return null;
	}

	protected UserInfo getUserInfoByToken(String localToken) {
		// 拿localToken换serverToken
		String userName = findUserName(localToken);
		//String key = "@@" + userName.split("@@")[1];
		//String serverToken = getServerToken(localToken + key);
		//Map<String, String> serverInfoMap = getLocalLoginUserInfo(serverToken + key);
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(userName))
				&& !Objects.isNull(userInfoCache.get(userName).get())) {
			UserInfo userInfo = (UserInfo) userInfoCache.get(userName).get();
			userInfo.getSysUser().setUsername(userName.contains("@@") ? userName.split("@@")[0] : userName);
			return userInfo;
		} else {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
	}

	protected String findUserName(String token) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(oAuth2AccessToken);
		// 清空用户信息
		PigUser userDetails = (PigUser) cacheManager.getCache(CacheConstants.USER_DETAILS).get(auth2Authentication.getName()).get();
		if (Objects.isNull(userDetails)) {
			throw new SSOBusinessException("登录异常！请重新登录");
		}
		return userDetails.getUsername();
	}

	protected UserInfo getUserInfo(String userName, String sysClass) {
		String key = userName + "@@" + sysClass;
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(key))
				&& !Objects.isNull(userInfoCache.get(key).get())) {
			return (UserInfo) userInfoCache.get(key).get();
		}
		return null;
	}

	// 拿ssoClientInfo
	protected Map getSSOClientInfo() {
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		return ossClientInfoMap;
	}
}