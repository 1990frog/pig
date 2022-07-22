package com.pig4cloud.pig.admin.sso.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ClassName CorsMapping
 * @Author Duys
 * @Description TODO
 * @Date 2022/7/21 17:49
 **/
public class CorsMapping extends WebMvcConfigurationSupport {
	/**
	 * 跨域访问配置接口
	 *
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
				.allowCredentials(true)
				.allowedHeaders("*")
				.maxAge(3600);
	}
}
