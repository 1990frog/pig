package com.pig4cloud.pig.common.sso.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @ClassName SSORouterFunctionConfiguration
 * @Author Duys
 * @Description
 * @Date 2022/7/20 17:05
 **/
public class SSORouterFunctionConfiguration implements ApplicationRunner {

	//private RouterFunction routerFunction;

	//private SSOConfigHandler ssoConfigHandler;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		//RouterFunction routerFunction = SpringContextHolder.getApplicationContext().getBean(RouterFunction.class);
		//SSOConfigHandler ssoConfigHandler = SpringContextHolder.getApplicationContext().getBean(SSOConfigHandler.class);
		//RouterFunction<ServerResponse> route = RouterFunctions.route(RequestPredicates.path("/sso").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), ssoConfigHandler);
		//routerFunction.and(route);
		//routerFunction.andRoute(RequestPredicates.path("/sso").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), ssoConfigHandler);
	}
}
