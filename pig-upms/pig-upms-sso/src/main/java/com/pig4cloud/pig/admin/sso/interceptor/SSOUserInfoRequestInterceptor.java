package com.pig4cloud.pig.admin.sso.interceptor;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.controller.SSOUserController;
import com.pig4cloud.pig.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @ClassName SSORequestInterceptor
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:42
 **/
@Component
@Slf4j
public class SSOUserInfoRequestInterceptor extends AbstractSSORequestInterceptor {

	@Autowired
	private SSOUserController ssoUserController;


	@Override
	public void afterPropertiesSet() {
		urls.add("/user/info");
		urls.add("/user/page");
		urls.add("/user/info/(.+)/(.+)");
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String curRequestPath = request.getServletPath();
		String method = request.getMethod();
		if (StrUtil.isEmpty(curRequestPath)) {
			return false;
		}
		// 对sso请求用户信息进行拦截
		if (!(requestMatches(curRequestPath) && (HttpMethod.GET.name().equals(method) && getSSOEnable()))) {
			return true;
		}
		R userInfo = null;
		ServletOutputStream outputStream = response.getOutputStream();
		response.setContentType("application/json;charset=utf-8");
		// 分发任务
		if (curRequestPath.matches("/user/info")) {
			userInfo = ssoUserController.info();
		} else if (curRequestPath.matches("/user/info/(.+)/(.+)")) {
			Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			String username = uriTemplateVars.get("username");
			String sysClass = uriTemplateVars.get("sysClass");
			userInfo = ssoUserController.infoNew(username, sysClass);
		} else if (curRequestPath.matches("/user/page")) {
			Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			String username = uriTemplateVars.get("username");
			String currentStr = uriTemplateVars.get("current");
			String sizeStr = uriTemplateVars.get("size");
			Long current = StrUtil.isEmpty(currentStr) ? 1 : Long.valueOf(currentStr);
			Long size = StrUtil.isEmpty(sizeStr) ? 1 : Long.valueOf(sizeStr);
			// current size
			userInfo = ssoUserController.getUserPage(username, current, size);
		}
		try {
			outputStream.write(processResponse(userInfo));
			response.setStatus(HttpStatus.OK.value());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("sso登录，获取用户信息失败！");
			//throw new SSOBusinessException("登录异常，获取用户信息失败！");
			outputStream.write("登录过期,请重新登录(GetInfo)!".getBytes(StandardCharsets.UTF_8));
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return false;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
}
