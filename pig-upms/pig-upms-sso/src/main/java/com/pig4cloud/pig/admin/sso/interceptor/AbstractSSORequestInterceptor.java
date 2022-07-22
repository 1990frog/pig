package com.pig4cloud.pig.admin.sso.interceptor;

import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @ClassName AbstractSSORequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/22 14:23
 **/
public abstract class AbstractSSORequestInterceptor implements HandlerInterceptor, InitializingBean {

	@Autowired
	private CacheManager cacheManager;

	// 提供公共的实现获取cache
	private Map ssoClientInfo;

	@Override
	public void afterPropertiesSet() {
		ssoClientInfo = getSSOClientInfo();
	}

	@Override
	public abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;


	// 拿ssoClientInfo
	private Map getSSOClientInfo() {
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		return ossClientInfoMap;
	}

	public boolean getSSOEnable() {
		return ssoClientInfo.containsKey("enable") ? (boolean) ssoClientInfo.get("enable") : false;
	}
}
