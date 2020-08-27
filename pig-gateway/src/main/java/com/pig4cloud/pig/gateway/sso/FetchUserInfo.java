package com.pig4cloud.pig.gateway.sso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Liaopan on 2020-08-25.
 */
@Component
@RequiredArgsConstructor
public class FetchUserInfo {

	private final SSOClientInfo ssoClientInfo;

	private final RestTemplate restTemplate;

	public boolean verify(String token) {
		return true;
	}
}
