package com.pig4cloud.pig.auth.config;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

public class PasswordTokenGranter extends ResourceOwnerPasswordTokenGranter {

	private AuthenticationManager authenticationManager;

	public PasswordTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(authenticationManager, tokenServices, clientDetailsService, requestFactory);
		this.authenticationManager = authenticationManager;
	}

	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
		String username = (String)parameters.get("username");
		String password = (String)parameters.get("password");
		parameters.remove("password");
		Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
		((AbstractAuthenticationToken)userAuth).setDetails(parameters);

		try {
			userAuth = this.authenticationManager.authenticate(userAuth);
		} catch (AccountStatusException var8) {
			throw new InvalidGrantException(var8.getMessage());
		} catch (BadCredentialsException var9) {
			throw new InvalidGrantException(var9.getMessage());
		}

		if (userAuth != null && userAuth.isAuthenticated()) {
			OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
			return new OAuth2Authentication(storedOAuth2Request, userAuth);
		} else {
			throw new InvalidGrantException("Could not authenticate user: " + username);
		}
	}
}
