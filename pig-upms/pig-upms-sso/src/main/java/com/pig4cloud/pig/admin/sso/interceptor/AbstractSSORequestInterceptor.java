package com.pig4cloud.pig.admin.sso.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AbstractSSORequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/22 14:23
 **/
@Slf4j
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
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		if (ssoClientInfo == null && ossClientInfo != null) {
			log.info("获取ssoClientInfo ");
			Cache.ValueWrapper valueWrapper = ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO);
			ssoClientInfo = valueWrapper == null ? null : (Map) valueWrapper.get();
			return;
		}
		// 可能因为配置信息变更了
		if (ssoClientInfo != null && (ossClientInfo == null || ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO) == null)) {
			log.info("释放ssoClientInfo ");
			ssoClientInfo = null;
		}
	}

	protected boolean getSSOEnable() {
		getSSOClientInfo();
		return ssoClientInfo == null ? false : (ssoClientInfo.containsKey("enable") ? (boolean) ssoClientInfo.get("enable") : false);
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

	protected byte[] processResponse(R r) {
		if (r == null) {
			return new byte[0];
		}
		JSONObject obj = JSONUtil.parseObj(r, false);
		if (!obj.containsKey("status")) {
			obj.putOnce("status", obj.get("code"));
			obj.putOnce("message", obj.get("msg"));
			obj.remove("code");
			obj.remove("msg");
		}
		String res = JSONUtil.toJsonPrettyStr(obj);
		return res.getBytes(StandardCharsets.UTF_8);
	}
}
