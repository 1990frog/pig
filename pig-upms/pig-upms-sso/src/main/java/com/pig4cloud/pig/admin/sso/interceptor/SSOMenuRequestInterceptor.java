package com.pig4cloud.pig.admin.sso.interceptor;

import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.controller.SSOMenuController;
import com.pig4cloud.pig.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName SSOMenuRequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:52
 **/
@Component
@Slf4j
public class SSOMenuRequestInterceptor extends AbstractSSORequestInterceptor {

	@Autowired
	private SSOMenuController ssoMenuController;


	@Override
	public void afterPropertiesSet() {
		urls.add("/menu");
		urls.add("/menu/tree/(.+)");
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String curRequestPath = request.getServletPath();
		String method = request.getMethod();
		if (StrUtil.isEmpty(curRequestPath)) {
			return false;
		}
		// 拦截的是/menu 并且是get请求，我才需要
		// /tree/{roleId}
		if (!(requestMatches(curRequestPath) && (HttpMethod.GET.name().equals(method) && getSSOEnable()))) {
			return true;
		}
		ServletOutputStream outputStream = null;
		try {
			// 这里两个请求再sso开启的时候使用同一个处理
			R userMenu = ssoMenuController.getUserMenu();
			outputStream = response.getOutputStream();
			outputStream.write(processResponse(userMenu));
			response.setContentType("application/json;charset=utf-8");
			response.setStatus(HttpStatus.OK.value());
			return false;
		} catch (Exception e) {
			log.error("sso登录，获取菜单失败！");
			throw new SSOBusinessException("登录异常，获取用户菜单失败！");
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

}
