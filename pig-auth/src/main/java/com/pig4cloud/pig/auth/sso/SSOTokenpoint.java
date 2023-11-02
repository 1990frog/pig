package com.pig4cloud.pig.auth.sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SSOTokenpoint
 * @Author Duys
 * @Description
 * @Date 2022/7/26 10:53
 **/
@RestController
@RequestMapping("/token/innerLogin")
public class SSOTokenpoint {

	@Autowired
	private SSOTokenGranter ssoTokenGranter;


	@GetMapping()
	public Object login(@RequestParam(name = "username") String username,
						@RequestParam(name = "userCode") String userCode,
						@RequestParam(name = "sysClass") String sysClass,
						@RequestParam(name = "password") String password,
						@RequestParam(name = "grant_type") String grant_type,
						@RequestParam(name = "scope") String scope,
						@RequestParam(name = "token") String token,
						@RequestParam(name = "appCode") String appCode,
						@RequestParam(name = "appName") String appName) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("username", username);
		parameters.put("password", password);
		parameters.put("userCode", userCode);
		parameters.put("grant_type", grant_type);
		parameters.put("scope", scope);
		// 准备做一个两边token的缓存
		parameters.put("token", token);
		parameters.put("appCode", appCode);
		parameters.put("appName", appName);
		parameters.put("sysClass", sysClass);
		parameters.put("timestamp", String.valueOf(System.nanoTime()));
		return ssoTokenGranter.initToken(parameters);
	}
}
