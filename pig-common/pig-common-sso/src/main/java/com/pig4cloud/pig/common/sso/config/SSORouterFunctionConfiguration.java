package com.pig4cloud.pig.common.sso.config;

import com.pig4cloud.pig.common.sso.handler.SSOConfigHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;

/**
 * @ClassName SSORouterFunctionConfiguration
 * @Author Duys
 * @Description
 * @Date 2022/7/20 17:05
 **/
@Component
@RequiredArgsConstructor
public class SSORouterFunctionConfiguration implements InitializingBean {

	private final RouterFunction routerFunction;

	private final SSOConfigHandler ssoConfigHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
		routerFunction.andRoute(RequestPredicates.path("/sso").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), ssoConfigHandler);
	}
}
