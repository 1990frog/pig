package com.pig4cloud.pig.admin.sso.config;

import com.pig4cloud.pig.admin.sso.interceptor.SSOMenuRequestInterceptor;
import com.pig4cloud.pig.admin.sso.interceptor.SSOUserInfoRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName InterceptorConfig
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:49
 **/
@Component
public class SSOInterceptorConfig implements WebMvcConfigurer {

	@Autowired
	private SSOUserInfoRequestInterceptor ssoUserInfoRequestInterceptor;

	@Autowired
	private SSOMenuRequestInterceptor ssoMenuRequestInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(ssoUserInfoRequestInterceptor).addPathPatterns("/user/info/**");
		registry.addInterceptor(ssoUserInfoRequestInterceptor).addPathPatterns("/user/extend/**");
		registry.addInterceptor(ssoMenuRequestInterceptor).addPathPatterns("/menu");
	}

}
