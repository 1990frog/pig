package com.pig4cloud.pig.admin.sso.interceptor;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName SSORequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:42
 **/
@Component
public class SSOUserInfoRequestInterceptor extends AbstractSSORequestInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		return true;
	}
}
