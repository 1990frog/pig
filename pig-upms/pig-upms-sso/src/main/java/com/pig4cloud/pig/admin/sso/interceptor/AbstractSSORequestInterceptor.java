package com.pig4cloud.pig.admin.sso.interceptor;

import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
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

	// 公共的urls
	protected List<String> urls = new ArrayList<>();

	@Override
	public abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;


	// 拿ssoClientInfo
	private void getSSOClientInfo() {
		if (ssoClientInfo == null) {
			Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
			ssoClientInfo = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		}
	}

	public boolean getSSOEnable() {
		getSSOClientInfo();
		return ssoClientInfo.containsKey("enable") ? (boolean) ssoClientInfo.get("enable") : false;
	}

	// 来校验是否存在要过滤的
	protected boolean requestMatches(String requestUrl) {
		boolean result = false;
		for (String url : urls) {
			if (StrUtil.isEmpty(url)) {
				continue;
			}
			result = requestUrl.matches(url);
			if (result) {
				return result;
			}
		}
		return result;
	}
}
