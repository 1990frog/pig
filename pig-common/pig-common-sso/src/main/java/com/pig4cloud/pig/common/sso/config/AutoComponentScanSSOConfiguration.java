package com.pig4cloud.pig.common.sso.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName AutoComponentScanSSOConfiguration
 * @Author Duys
 * @Description
 * @Date 2022/7/20 16:25
 **/
@ComponentScan(basePackages = {"com.pig4cloud.pig.common.sso"})
@Configuration
public class AutoComponentScanSSOConfiguration {

	/*@Autowired
	private RouterFunction routerFunction;
	@Autowired
	private SSOConfigHandler ssoConfigHandler;

	@PostConstruct
	public void init() {
		routerFunction.andRoute(RequestPredicates.path("/sso").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), ssoConfigHandler);
	}*/
}
