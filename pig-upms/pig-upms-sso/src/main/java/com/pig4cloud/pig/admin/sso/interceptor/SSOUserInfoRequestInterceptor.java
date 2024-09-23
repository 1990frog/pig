package com.pig4cloud.pig.admin.sso.interceptor;

import cn.hutool.core.util.StrUtil;
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
		urls.add("/user/extend/page");
		urls.add("/user/info/(.+)/(.+)");
		urls.add("/role/info/list/(.+)");
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
		try {
			if (curRequestPath.matches("/user/info")) {
				log.info("通过/user/info 接口获取用户信息");
				userInfo = ssoUserController.info();
				outputStream.write(processResponse(userInfo));
			} else if (curRequestPath.matches("/user/info/(.+)/(.+)")) {
				log.info("通过/user/infoNew 接口获取用户信息");
				Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
				String username = uriTemplateVars.get("username");
				String sysClass = uriTemplateVars.get("sysClass");
				userInfo = ssoUserController.infoNew(username, sysClass);
				outputStream.write(processResponse(userInfo));
			} else if (curRequestPath.matches("/user/extend/page")) {
				Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
				log.info("通过/user/extend/page 接口获取用户信息 参数信息={}", uriTemplateVars);
				if (uriTemplateVars.isEmpty()) {
					Map<String, String[]> parameterMap = request.getParameterMap();
					parameterMap.keySet().stream().forEach(s -> {
						if (parameterMap.get(s) != null && parameterMap.get(s).length > 0) {
							uriTemplateVars.put(s, parameterMap.get(s)[0]);
						}
					});
				}
				String keyword = uriTemplateVars.get("keyword");
				String currentStr = uriTemplateVars.get("current");
				String sizeStr = uriTemplateVars.get("size");
				Long current = StrUtil.isEmpty(currentStr) ? 1 : Long.valueOf(currentStr);
				Long size = StrUtil.isEmpty(sizeStr) ? 20 : Long.valueOf(sizeStr);
				// current size
				userInfo = ssoUserController.getUserExtendPage(keyword, current, size);
				outputStream.write(processResponsePage(userInfo));
			} else if (curRequestPath.matches("/role/info/list/all")) {
				R roleAll = ssoUserController.getRoleAll();
				outputStream.write(processResponse(roleAll));
			} else if (curRequestPath.matches("/role/info/list/current")) {
				R roleAll = ssoUserController.getRoleCurrent();
				outputStream.write(processResponse(roleAll));
			}
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
