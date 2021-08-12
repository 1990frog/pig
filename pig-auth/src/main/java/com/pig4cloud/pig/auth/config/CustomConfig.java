package com.pig4cloud.pig.auth.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomConfig{

	@Autowired
	private AuthorizationServerConfig config;

	@Autowired
	private AuthenticationManager manager;

	private static final String CLIENT_ID = "test";

	public OAuth2AccessToken initToken(Map<String, String> parameters){
		OAuth2RequestFactory factory = config.getEndpoints().getOAuth2RequestFactory();
		AuthorizationServerTokenServices services = config.getEndpoints().getDefaultAuthorizationServerTokenServices();
		ClientDetails clientDetails = config.getEndpoints().getClientDetailsService().loadClientByClientId(CLIENT_ID);
		TokenRequest tokenRequest = createRequest(parameters);
		OAuth2Request storedOAuth2Request = factory.createOAuth2Request(clientDetails, tokenRequest);
		OAuth2Authentication oAuth2Authentication = new  OAuth2Authentication(storedOAuth2Request, userAuthentication(tokenRequest));
		return services.createAccessToken(oAuth2Authentication);
	}

	@SneakyThrows
	private Authentication userAuthentication(TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
		String username = parameters.get("username");
		String password = parameters.get("password");
		parameters.remove("password");
		Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
		((AbstractAuthenticationToken)userAuth).setDetails(parameters);
		return this.manager.authenticate(userAuth);
	}

	private TokenRequest createRequest(Map<String, String> parameters){
		OAuth2RequestFactory factory = config.getEndpoints().getOAuth2RequestFactory();
		ClientDetails clientDetails = config.getEndpoints().getClientDetailsService().loadClientByClientId(CLIENT_ID);
		return factory.createTokenRequest(parameters, clientDetails);
	}
}
