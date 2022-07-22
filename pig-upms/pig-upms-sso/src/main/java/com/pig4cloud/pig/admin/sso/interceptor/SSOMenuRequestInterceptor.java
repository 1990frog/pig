package com.pig4cloud.pig.admin.sso.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName SSOMenuRequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:52
 **/
public class SSOMenuRequestInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		return false;
	}
}
