package com.pig4cloud.pig.common.sso.component;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SSOClientInfo
 * @Author Duys
 * @Description 目前依然使用配置的形式来解决
 * @Date 2022/7/20 14:37
 **/
@ToString
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "sso")
public class SSOClientInfo implements Serializable {
	private static final long serialVersionUID = 756289259126028558L;

	private boolean enable = false;

	private String ssoHost;

	private Integer type = 2; // 1-soap1.1(230 SSO) ,2-soap1.2

	private List<String> apps;

	private String serverUrl;

	private String verifyUrl;

	private String logoutUrl;

	private String userServiceUrl;

	private String getUserInfo;

	private String defaultUserCode;

	private String oauthTokenUrl;

	/**
	 * 登录密钥
	 */
	private String cryptogram;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getSsoHost() {
		return ssoHost;
	}

	public void setSsoHost(String ssoHost) {
		this.ssoHost = ssoHost;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getVerifyUrl() {
		return verifyUrl;
	}

	public void setVerifyUrl(String verifyUrl) {
		this.verifyUrl = verifyUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getUserServiceUrl() {
		return userServiceUrl;
	}

	public void setUserServiceUrl(String userServiceUrl) {
		this.userServiceUrl = userServiceUrl;
	}

	public String getGetUserInfo() {
		return getUserInfo;
	}

	public void setGetUserInfo(String getUserInfo) {
		this.getUserInfo = getUserInfo;
	}

	public String getDefaultUserCode() {
		return defaultUserCode;
	}

	public void setDefaultUserCode(String defaultUserCode) {
		this.defaultUserCode = defaultUserCode;
	}

	public String getOauthTokenUrl() {
		return oauthTokenUrl;
	}

	public void setOauthTokenUrl(String oauthTokenUrl) {
		this.oauthTokenUrl = oauthTokenUrl;
	}

	public String getCryptogram() {
		return cryptogram;
	}

	public void setCryptogram(String cryptogram) {
		this.cryptogram = cryptogram;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
}
