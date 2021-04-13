package com.clinbrain.dip;

import com.clinbrain.dip.rest.request.RequestJsonHandlerMethodArgumentResolver;
import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.List;

/**
 * @author pig archetype
 * <p>
 * 项目启动类
 */
@EnablePigSwagger2
@EnablePigFeignClients
@EnablePigResourceServer
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
public class ETLWebApplication extends WebMvcConfigurerAdapter{

    public static void main(String[] args) {
        SpringApplication.run(ETLWebApplication.class, args);
    }

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    	argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver());
		super.addArgumentResolvers(argumentResolvers);
	}
}
