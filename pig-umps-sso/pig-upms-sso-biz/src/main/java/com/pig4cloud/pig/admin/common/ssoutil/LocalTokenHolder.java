package com.pig4cloud.pig.admin.common.ssoutil;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @ClassName LocalTokenHolder
 * @Author Duys
 * @Description
 * @Date 2021/12/15 17:49
 **/
public class LocalTokenHolder {

	public static String getToken() {
		try {
			// 根据token获取
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = requestAttributes.getRequest();
			Enumeration<String> headers = request.getHeaders("Authorization");
			while (headers.hasMoreElements()) {
				String value = headers.nextElement();
				if ((value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase()))) {
					String authHeaderValue = value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
					request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE,
							value.substring(0, OAuth2AccessToken.BEARER_TYPE.length()).trim());
					int commaIndex = authHeaderValue.indexOf(',');
					if (commaIndex > 0) {
						authHeaderValue = authHeaderValue.substring(0, commaIndex);
					}
					return authHeaderValue;
				}
			}
		} catch (Exception e) {

		}
		return null;
	}
}
