package com.clinbrain.dip;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author pig archetype
 * <p>
 * 项目启动类
 */
@EnablePigResourceServer
@EnablePigFeignClients
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
