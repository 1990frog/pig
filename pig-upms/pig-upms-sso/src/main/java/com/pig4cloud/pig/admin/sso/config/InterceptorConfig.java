package com.pig4cloud.pig.admin.sso.config;

import com.pig4cloud.pig.admin.sso.interceptor.SSOMenuRequestInterceptor;
import com.pig4cloud.pig.admin.sso.interceptor.SSOUserInfoRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ClassName InterceptorConfig
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:49
 **/
@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

	@Autowired
	private SSOUserInfoRequestInterceptor ssoUserInfoRequestInterceptor;

	@Autowired
	private SSOMenuRequestInterceptor ssoMenuRequestInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(ssoUserInfoRequestInterceptor).addPathPatterns("/user/info/**");
		registry.addInterceptor(ssoMenuRequestInterceptor).addPathPatterns("/menu");
		super.addInterceptors(registry);
	}
}
