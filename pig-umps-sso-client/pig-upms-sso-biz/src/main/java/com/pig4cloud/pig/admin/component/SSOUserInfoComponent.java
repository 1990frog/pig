package com.pig4cloud.pig.admin.component;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.annotation.SSOUserInfo;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.common.ssoutil.LocalTokenHolder;
import com.pig4cloud.pig.admin.common.ssoutil.UserRoleInfoParse;
import com.pig4cloud.pig.admin.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.model.SSOPrivilege;
import com.pig4cloud.pig.admin.model.SSORoleInfo;
import com.pig4cloud.pig.admin.model.SoapEntity;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.security.service.PigUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName SSOUserInfoComponent
 * @Author Duys
 * @Description
 * @Date 2021/12/13 14:32
 **/

@Slf4j
/*@Aspect
@Component*/
public class SSOUserInfoComponent {

	@Autowired
	private ApplicationContext applicationContext;


	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 用于SpEL表达式解析 .
	 */


	private SpelExpressionParser parser;
	/**
	 * 用于获取方法参数定义名字 .
	 */


	private DefaultParameterNameDiscoverer nameDiscoverer;

	private PasswordEncoder ENCODER;

	@PostConstruct
	public void init() {
		parser = new SpelExpressionParser();
		nameDiscoverer = new DefaultParameterNameDiscoverer();
		ENCODER = new BCryptPasswordEncoder();
	}


	@Around("@annotation(ssoUserInfo)")
	public Object process(ProceedingJoinPoint pjp, SSOUserInfo ssoUserInfo) {
		Method method = getMethod(pjp);
		if (!Objects.isNull(method) && method.isAnnotationPresent(SSOUserInfo.class)) {
			// 开始做业务了
			String localToken = LocalTokenHolder.getToken();
			// 可能是内部本地请求
			if (StringUtils.isEmpty(localToken)) {
				String usernameParam = ssoUserInfo.userName();
				String sysClassParam = ssoUserInfo.sysClass();
				String username = generateKeyBySpEL(usernameParam, pjp);
				String sysClass = generateKeyBySpEL(sysClassParam, pjp);
				if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(sysClass)) {
					String key = username + "@@" + sysClass;
					Cache cache = cacheManager.getCache(CacheConstants.SSO_USERNAME_INFO);
					if (Objects.isNull(cache) || Objects.isNull(cache.get(key)) || Objects.isNull(cache.get(key).get())) {
						throw new RuntimeException("会话过期！请重新登录！");
					}
					Map localLoginMap = (Map) cache.get(key).get();
					//  look: SSOTokenGlobalFilter -> filter() -> cacheUsernameAndSysCode()
					localToken = (String) localLoginMap.getOrDefault("value", null);
					if (StringUtils.isEmpty(localToken)) {
						return loginFindUserInfo(username, sysClass);
					}
				}
			}
			// 1.从缓存获取用户基本信息;
			// 如果没有token，说明是第一次登录来请求的用户信息
			// 2.获取server端的token
			String serverToken = getServerToken(localToken);
			// 3.根据serverToken去sso服务端拿到的用户信息
			Map userInfoMap = getServerTokenToSSOUserInfo(serverToken);
			// 4.sso用户登录使用的参数
			Map ssoClientInfoMap = getSSOClientInfo(serverToken);
			// 5.sso用户模拟本地登录使用的参数，也就是serverToken 换取localToken的时候用的参数
			Map<String, String> serverInfoMap = getLocalLoginUserInfo(serverToken);
			// 6.是否需要走一次登录
			if (Objects.isNull(userInfoMap)) {
				// 登录，然后获取所有的
				userInfoMap = getUserInfoFromSSOServer(serverToken, serverInfoMap, ssoClientInfoMap);
			}
			// 7.拿userInfo
			UserInfo userInfo = getLocalUserInfo(serverToken, serverInfoMap, ssoClientInfoMap);
			// 这是要返回的
			return userInfo;
		}
		return null;
	}

	private UserInfo loginFindUserInfo(String userName, String sysClass) {
		// 获取登录的信息
		String key = userName + "@@" + sysClass;
		// 1.拿servertoken
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN);
		// 2.获取ssoClientInfo
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO);
		String serverToken = (String) ssoClientInfoCache.get(key).get();

		// 	用serverToken 换用户的登录信息
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		Map<String, String> serverInfoMap = (Map<String, String>) cache.get(serverToken).get();
		return getLocalUserInfo(serverToken, serverInfoMap, ossClientInfoMap);
	}

	/**
	 * @param serverInfoMap
	 */


	private UserInfo getLocalUserInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfoMap) {
		UserInfo userInfo = null;
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (Objects.isNull(userInfoCache) || Objects.isNull(userInfoCache.get(serverInfoMap.get("username")))
				|| Objects.isNull(userInfoCache.get(serverInfoMap.get("username")).get())) {

			// 去远端拿信息 - 走webService 并且拼装PigUser放缓存
			List<SSORoleInfo> ssoRoleInfos = getSSORoleInfo(serverToken, serverInfoMap, ssoClientInfoMap);
			List<SSOPrivilege> ssoPrivileges = getSSOPrivilege(serverToken, serverInfoMap, ssoClientInfoMap);
			// userDetails
			fillPigUser(serverInfoMap, ssoRoleInfos, ssoPrivileges);
			// 组装一下userInfo
			userInfo = fillUserInfo(serverInfoMap, ssoRoleInfos, ssoPrivileges);
		} else {
			userInfo = (UserInfo) userInfoCache.get(serverInfoMap.get("username")).get();
		}
		return userInfo;
	}

	private Map<String, String> getLocalLoginUserInfo(String serverToken) {
		Cache serverInfo = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		if (Objects.isNull(serverInfo) || Objects.isNull(serverInfo.get(serverToken))
				|| Objects.isNull(serverInfo.get(serverToken).get())) {
			// 登录，然后获取所有的
			throw new RuntimeException("会话过期！请重新登录！");
		}
		return (Map<String, String>) serverInfo.get(serverToken).get();
	}

	/**
	 * 获取配置的ssoClientInfo ->getaway配置的
	 *
	 * @return
	 */


	private Map getSSOClientInfo(String serverToken) {
		Cache ssoClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		if (Objects.isNull(ssoClientInfo) || Objects.isNull(ssoClientInfo.get(serverToken))
				|| Objects.isNull(ssoClientInfo.get(serverToken).get())) {
			throw new RuntimeException("会话过期！请重新登录！");
		}
		Map ssoClientInfoMap = (Map) ssoClientInfo.get(serverToken).get();
		return ssoClientInfoMap;
	}

	/**
	 * 这是登录的时候，获取到的用户信息 ，从sso、server端获取的
	 */


	private Map getServerTokenToSSOUserInfo(String serverToken) {
		Cache serverTokenUserInfo = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		// 先来看看userInfo的信息-用户的基本信息
		Map userInfoMap = null;
		if (!Objects.isNull(serverTokenUserInfo.get(serverToken)) && !Objects.isNull(serverTokenUserInfo.get(serverToken).get())) {
			userInfoMap = (Map) serverTokenUserInfo.get(serverToken).get();
		}
		return userInfoMap;
	}

	/**
	 * 需要拿到本地的token，需要注意可能是内部请求，
	 * 所以需要根据 自定义注解参数获取
	 */


	private String getLocalToken(ProceedingJoinPoint pjp, SSOUserInfo ssoUserInfo) {
		// localToken
		String localToken = LocalTokenHolder.getToken();
		// 可能是内部调用，没有token，那么就根据userName来获取一下token
		if (StringUtils.isEmpty(localToken)) {
			String usernameParam = ssoUserInfo.userName();
			String sysClassParam = ssoUserInfo.sysClass();
			String username = generateKeyBySpEL(usernameParam, pjp);
			String sysClass = generateKeyBySpEL(sysClassParam, pjp);
			if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(sysClass)) {
				String key = username + "@@" + sysClass;
				Cache cache = cacheManager.getCache(CacheConstants.SSO_USERNAME_INFO);
				if (Objects.isNull(cache) || Objects.isNull(cache.get(key)) || Objects.isNull(cache.get(key).get())) {
					throw new RuntimeException("会话过期！请重新登录！");
				}
				Map localLoginMap = (Map) cache.get(key).get();
				//  look: SSOTokenGlobalFilter -> filter() -> cacheUsernameAndSysCode()
				localToken = (String) localLoginMap.getOrDefault("value", null);
			}
		}
		return localToken;
	}

	/**
	 * 获取server 端的token，在登录的时候做了映射关系
	 */


	private String getServerToken(String localToken) {
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		if (Objects.isNull(serverTokenCache) || Objects.isNull(serverTokenCache.get(localToken))
				|| Objects.isNull(serverTokenCache.get(localToken).get())) {
			throw new RuntimeException("会话过期！请重新登录！");
		}
		String serverToken = (String) serverTokenCache.get(localToken).get();
		return serverToken;
	}

	private UserInfo fillUserInfo(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new RuntimeException("用户信息为空，请重新登录");
		}
		// 再拿一次
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && Objects.isNull(userInfoCache.get(serverInfoMap.get("username")))
				&& Objects.isNull(userInfoCache.get(serverInfoMap.get("username")).get())) {
			return (UserInfo) userInfoCache.get(serverInfoMap.get("username")).get();
		}
		String userCode = username.split("@@")[0];
		String sysClass = username.split("@@")[1];
		SysUser sysUser = new SysUser();
		sysUser.setUserId(1);
		sysUser.setDeptId(1);
		sysUser.setDelFlag("0");
		sysUser.setLockFlag("0");
		sysUser.setUsername(userCode);
		sysUser.setSysClass(sysClass);
		sysUser.setPassword(ENCODER.encode(serverInfoMap.get("password")));
		UserInfo userInfo = new UserInfo();
		userInfo.setSysUser(sysUser);
		// 设置角色列表 （ID）
		if (!org.springframework.util.CollectionUtils.isEmpty(ssoUserInfos)) {
			userInfo.setSsoRoles(ArrayUtil.toArray(ssoUserInfos.stream().map(SSORoleInfo::getRoleCode).collect(Collectors.toList()), String.class));
		}
		// 设置权限列表（menu.permission）
		if (!org.springframework.util.CollectionUtils.isEmpty(privileges)) {
			Set<String> pris = new HashSet<>();
			privileges.stream().filter(pri -> pri != null && pri.getExtPropertyInfo() != null)
					.forEach(ssoPrivilege -> pris.add(ssoPrivilege.getExtPropertyInfo().getPrivilege_Property_URL()));
			userInfo.setPermissions(ArrayUtil.toArray(pris, String.class));
		}
		if (userInfoCache != null) {
			userInfoCache.put(serverInfoMap.get("username"), userInfo);
		}
		return userInfo;
	}

	private PigUser fillPigUser(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new RuntimeException("用户信息为空，请重新登录");
		}

		Cache userDetailsCache = cacheManager.getCache(CacheConstants.USER_DETAILS);
		if (!Objects.isNull(userDetailsCache) && Objects.isNull(userDetailsCache.get(serverInfoMap.get("username")))
				&& Objects.isNull(userDetailsCache.get(serverInfoMap.get("username")).get())) {
			return (PigUser) userDetailsCache.get(serverInfoMap.get("username")).get();
		}

		String userCode = username.split("@@")[0];
		String sysClass = username.split("@@")[1];
		Set<String> dbAuthsSet = new HashSet<>();
		//获取用户系统标识
		dbAuthsSet.add(SecurityConstants.SYS_CLASS + sysClass);
		if (ArrayUtil.isNotEmpty(ssoUserInfos)) {
			// 获取角色
			ssoUserInfos.stream().forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role.getRoleCode()));
		}
		if (ArrayUtil.isNotEmpty(privileges)) {
			// 获取资源
			privileges.stream().filter(pri -> pri.getExtPropertyInfo() != null && pri.getExtPropertyInfo().getPrivilege_Property_URL() != null)
					.forEach(pri -> dbAuthsSet.add(pri.getExtPropertyInfo().getPrivilege_Property_URL()));
		}
		Collection<? extends GrantedAuthority> authorities = AuthorityUtils
				.createAuthorityList(dbAuthsSet.toArray(new String[0]));

		// userId 和 deptId 两边系统有所差异，这儿默认给一个
		PigUser userDetails = new PigUser(1, 1, sysClass, userCode,
				SecurityConstants.BCRYPT + serverInfoMap.get("password"),
				true, true, true, true, authorities);
		if (userDetailsCache != null) {
			userDetailsCache.put(serverInfoMap.get("username"), userDetails);
		}
		return userDetails;
	}

	/**
	 * 获取用户的权限和角色信息
	 *
	 * @param serverToken   sso
	 *                      服务端token
	 * @param serverInfoMap sso
	 *                      登录的时候使用的参数
	 * @param ssoClientInfo sso
	 *                      配置信息，在getaway上
	 * @return 本地的用户
	 */


	private List<SSORoleInfo> getSSORoleInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new RuntimeException("用户信息为空，请重新登录");
		}
		String userCode = username.split("@@")[0];
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(serverInfoMap.get("appCode"));
		soapEntity.setAppName(serverInfoMap.get("appName"));
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);

		// 设置一下wsdl的路径
		String url = (String) ssoClientInfo.get("serverUrl");
		String wsdlUrl = null;
		try {
			URL serverUrl = new URL(url);
			String host = serverUrl.getHost();
			int port = serverUrl.getPort();
			if (host.contains("http://")) {
				wsdlUrl = host + ":" + port;
			} else {
				wsdlUrl = "http://" + host + ":" + port;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		soapEntity.setHost(wsdlUrl);
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject roleInfo = WebServiceHttpClient.post(soapEntity);
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSORoleInfo> roleInfos = roleInfoParse.parse(roleInfo, SSORoleInfo.class, SoapTypeEnum.SOAP_ROLE);
		return roleInfos;
	}

	/**
	 * 获取用户的权限和角色信息
	 *
	 * @param serverToken   sso
	 *                      服务端token
	 * @param serverInfoMap sso
	 *                      登录的时候使用的参数
	 * @param ssoClientInfo sso
	 *                      配置信息，在getaway上
	 * @return 本地的用户
	 */


	private List<SSOPrivilege> getSSOPrivilege(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new RuntimeException("用户信息为空，请重新登录");
		}
		String userCode = username.split("@@")[0];
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(serverInfoMap.get("appCode"));
		soapEntity.setAppName(serverInfoMap.get("appName"));
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);

		// 设置一下wsdl的路径
		String url = (String) ssoClientInfo.get("serverUrl");
		String wsdlUrl = null;
		try {
			URL serverUrl = new URL(url);
			String host = serverUrl.getHost();
			int port = serverUrl.getPort();
			if (host.contains("http://")) {
				wsdlUrl = host + ":" + port;
			} else {
				wsdlUrl = "http://" + host + ":" + port;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		soapEntity.setHost(wsdlUrl);
		// 请求角色
		// 请求权限
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject permissionInfo = WebServiceHttpClient.post(soapEntity);
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> privileges = roleInfoParse.parse(permissionInfo, SSOPrivilege.class, SoapTypeEnum.SOAP_PER);
		return privileges;
	}


	/**
	 * 去服务端拿用户信息吧
	 *
	 * @param serverToken
	 * @param parameters
	 * @param ssoClientInfoMap
	 * @return
	 */

	private Map getUserInfoFromSSOServer(String serverToken, Map<String, String> parameters, Map ssoClientInfoMap) {
		String appName = parameters.get("appName");
		String appCode = parameters.get("appCode");
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		if (cache != null && cache.get(serverToken) != null) {
			return (Map) cache.get(serverToken).get();
		}
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", serverToken);
		BASE64Encoder encoder = new BASE64Encoder();
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = encoder.encode(textByte);

		//初始化LocalDateTime对象
		ZoneOffset zoneOffset = ZoneOffset.ofHours(0);
		//初始化LocalDateTime对象
		LocalDateTime localDateTime = LocalDateTime.now();
		long TimeStamp = localDateTime.toEpochSecond(zoneOffset);
		String buffer = base64AppName + "|" + appCode + "|" + TimeStamp + "|" + serverToken;
		String Sign = SecureUtil.md5(buffer);
		String header = base64AppName + "|" + TimeStamp + "|" + Sign;

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + header);
		final HttpEntity<String> entity = new HttpEntity<String>(headers);
		final Map map = restTemplate.exchange((String) ssoClientInfoMap.get("getUserInfo") + "?token=" + serverToken,
				HttpMethod.GET, entity, Map.class).getBody();
		if (map != null && !map.keySet().isEmpty()) {
			cache.put(serverToken, map);
		} else {
			throw new RuntimeException("token过期！请重新登录！");
		}
		return map;
	}

	// 获取切入点的方法
	public Method getMethod(ProceedingJoinPoint pjp) {
		Object target = pjp.getTarget();
		// 获取方法
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		try {
			// 根据切入点获取方法具体
			method = target.getClass().getMethod(method.getName(), method.getParameterTypes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return method;
	}

	/**
	 * 解析注解中所带的参数
	 */


	public String generateKeyBySpEL(String spELString, ProceedingJoinPoint joinPoint) {
		if (StringUtils.isEmpty(spELString)) {
			return null;
		}
		// 通过joinPoint获取被注解方法
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		// 使用spring的DefaultParameterNameDiscoverer获取方法形参名数组
		String[] paramNames = nameDiscoverer.getParameterNames(method);
		// 解析过后的Spring表达式对象
		Expression expression = parser.parseExpression(spELString);
		// spring的表达式上下文对象
		EvaluationContext context = new StandardEvaluationContext();
		// 通过joinPoint获取被注解方法的形参
		Object[] args = joinPoint.getArgs();
		// 给上下文赋值
		for (int i = 0; i < args.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}
		return expression.getValue(context).toString();
	}
}
