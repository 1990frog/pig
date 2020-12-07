package com.clinbrain.dip;

import com.clinbrain.dip.rest.request.RequestJsonHandlerMethodArgumentResolver;
import com.pig4cloud.pig.common.security.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author pig archetype
 * <p>
 * 项目启动类
 */
@EnablePigFeignClients
@EnablePigResourceServer
@SpringCloudApplication
public class ETLWebWarApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ETLWebApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ETLWebWarApplication.class, args);
	}
}
