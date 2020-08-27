package com.pig4cloud.pig.gateway.sso;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Liaopan on 2020-08-25.
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso")
public class SSOClientInfo {

	private boolean enable = false;

	private String appName;

	private String serverUrl;

	private String verifyUrl;

	private String logoutUrl;

	private String userServiceUrl;

	private String getUserInfo;

	private String appCode;

	private String defaultUserCode;

	private String oauthTokenUrl;

	/**
	 * 登录密钥
	 */
	private String cryptogram;
}
