package com.pig4cloud.pig.admin.sso.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.entity.UserExtendInfo;
import com.pig4cloud.pig.admin.sso.common.constants.SSOWebServiceConstants;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SSOTypeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserRoleInfoParse;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.sso.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.sso.model.SSOPermissionExtPropertyInfo;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.model.SSORoleInfo;
import com.pig4cloud.pig.admin.sso.model.SoapEntity;
import com.pig4cloud.pig.admin.sso.service.IRemoteService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName RemoteServiceImpl
 * @Author Duys
 * @Description
 * @Date 2021/12/9 10:42
 **/
@Service
@Slf4j
public class RemoteServiceImpl implements IRemoteService {

	@Autowired
	protected CacheManager cacheManager;

	@Override
	public List<SSORoleInfo> getSSORoleInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String userCode = serverInfoMap.get("userCode");
		if (StrUtil.isEmpty(userCode)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		String sysClass = serverInfoMap.get("sysClass");
		String appCode = serverInfoMap.get("appCode");
		String appName = serverInfoMap.get("appName");
		boolean admin = isAdmin(serverToken, sysClass);
		Integer type = (Integer) ssoClientInfo.get("type");
		SSOTypeEnum ssoTypeEnum = SSOTypeEnum.parse(type == null ? 2 : type);
		// 就需要获取,当前app下的所有的角色信息
		if (admin || SSOTypeEnum.SOAP_1_1.equals(ssoTypeEnum)) {
			return findSSORoleInfoByHttp(admin, appCode, serverToken, ssoClientInfo);
		}
		return findSSORoleInfoBySoap(appCode, appName, userCode, sysClass, serverToken, ssoClientInfo);
	}

	/**
	 * 获取用户的权限和角色信息
	 *
	 * @param serverToken   sso 服务端token
	 * @param serverInfoMap sso 登录的时候使用的参数
	 * @param ssoClientInfo sso 配置信息，在getaway上
	 * @return 本地的用户
	 */
	@Override
	public List<SSOPrivilege> getSSOPrivilege(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {

		String userCode = serverInfoMap.get("userCode");
		if (StrUtil.isEmpty(userCode)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		String appCode = serverInfoMap.get("appCode");
		String appName = serverInfoMap.get("appName");
		String sysClass = serverInfoMap.get("sysClass");
		boolean admin = isAdmin(serverToken, sysClass);
		Integer type = (Integer) ssoClientInfo.get("type");
		SSOTypeEnum ssoTypeEnum = SSOTypeEnum.parse(type == null ? 2 : type);
		if (admin || SSOTypeEnum.SOAP_1_1.equals(ssoTypeEnum)) {
			log.info("是管理员用户，获取所有菜单,getSSOPrivilege()");
			return findSSOPriInfoByHttp(admin, userCode, appCode, serverToken, ssoClientInfo);
		}
		//String sysClass = serverInfoMap.get("sysClass");
		//JSONObject permissionInfo = null;
		/*String key = userCode + "@@" + sysClass;
		if (permissionInfo != null) {
			cacheUserPrivileges(key, permissionInfo);
		}*/
		JSONObject permissionInfo = getSSOUserInfo(userCode, appCode, appName, serverToken, ssoClientInfo);
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> privileges = roleInfoParse.parse(permissionInfo, SSOPrivilege.class, SoapTypeEnum.SOAP_PER);
		return privileges;
	}

	@Override
	public List<SSOPrivilege> getSSOMenus(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String userCode = serverInfoMap.get("userCode");
		String sysClass = serverInfoMap.get("sysClass");
		String appCode = serverInfoMap.get("appCode");
		String appName = serverInfoMap.get("appName");
		boolean admin = isAdmin(serverToken, sysClass);
		Integer type = (Integer) ssoClientInfo.get("type");
		SSOTypeEnum ssoTypeEnum = SSOTypeEnum.parse(type == null ? 2 : type);
		if (admin || SSOTypeEnum.SOAP_1_1.equals(ssoTypeEnum)) {
			log.info("是管理员用户，获取所有菜单，getSSOMenus()");
			List<SSOPrivilege> ssoPrivileges = findSSOPriInfoByHttp(admin, userCode, appCode, serverToken, ssoClientInfo);
			if (CollectionUtils.isEmpty(ssoPrivileges)) {
				return null;
			}
			return buildTree(ssoPrivileges);
		}
		// 这里需要按照另外的解析方式。。。
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> privileges = new ArrayList<>();
		// 一层一层的去解析
		JSONObject permissionInfo = getSSOUserInfo(userCode, appCode, appName, serverToken, ssoClientInfo);
		roleInfoParse.parseSSOMenu(permissionInfo, privileges);
		return privileges;
	}

	private List<SSOPrivilege> buildTree(List<SSOPrivilege> ssoPrivileges) {
		Map<Integer, List<SSOPrivilege>> privilegeMap = ssoPrivileges.stream().collect(Collectors.groupingBy(SSOPrivilege::getParentId));
		List<SSOPrivilege> parents = privilegeMap.get(-1);
		for (SSOPrivilege privilege : parents) {
			// 去找以当前id为父的下级
			nextNode(privilege, privilegeMap);
		}
		return parents;
	}

	private void nextNode(SSOPrivilege parent, Map<Integer, List<SSOPrivilege>> privilegeMap) {
		if (parent == null || parent.getId() == null) {
			return;
		}
		List<SSOPrivilege> nexts = privilegeMap.get(parent.getId());
		if (CollectionUtils.isEmpty(nexts)) {
			return;
		}
		parent.setSsoPrivileges(nexts);
		for (SSOPrivilege cur : nexts) {
			nextNode(cur, privilegeMap);
		}
	}

	@Override
	public Integer findUserCount(String userName, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setUserName(userName);
		soapEntity.setAppCode("");
		soapEntity.setAppName("");
		soapEntity.setSsoType(findSSOType(ssoClientInfo));
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE_TOTAL);
		processHostAndWsdl(soapEntity, ssoClientInfo);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject total = WebServiceHttpClient.get(soapEntity);
		if (total == null) {
			return null;
		}
		Integer totalInt = total.getInt("content");
		if (totalInt == null || totalInt.intValue() <= 0) {
			return null;
		}
		return totalInt;
	}

	@Override
	public List<UserExtendInfo> findUserInfo(String userName, Long current, Long size, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setCurrent(current);
		soapEntity.setSize(size);
		soapEntity.setUserName(userName);
		soapEntity.setAppCode("");
		soapEntity.setAppName("");
		soapEntity.setSsoType(findSSOType(ssoClientInfo));
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE);
		processHostAndWsdl(soapEntity, ssoClientInfo);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject users = WebServiceHttpClient.get(soapEntity);
		if (users == null) {
			return null;
		}
		try {
			JSONArray user = users.getJSONArray("User");
			if (user == null || user.size() <= 0) {
				return null;
			}
			List<UserExtendInfo> list = new ArrayList<>();
			for (int i = 0; i < user.size(); i++) {
				JSONObject object = (JSONObject) user.get(i);
				UserExtendInfo sysUser = new UserExtendInfo();
				sysUser.setUsername(object.getStr("UserName"));
				sysUser.setPhone(object.getStr("Mobile"));
				sysUser.setEmail(object.getStr("Email"));
				sysUser.setUserType(object.getStr("UserType"));
				sysUser.setUserCode(object.getStr("UserCode"));
				sysUser.setUserTypeName(object.getStr("UserTypeName"));
				sysUser.setDeptCode(object.getStr("DeptCode"));
				sysUser.setDeptName(object.getStr("DeptName"));
				list.add(sysUser);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IPage<UserExtendInfo> findUserInfo(Long current, Long size, String serverToken, String keyword, Map ssoClientInfo) {
		IPage<UserExtendInfo> res = new Page<>(current, size);
		try {
			SoapEntity soapEntity = new SoapEntity();
			soapEntity.setCurrent(current);
			soapEntity.setSize(size);
			soapEntity.setToken(serverToken);
			soapEntity.setAppCode("");
			soapEntity.setAppName("");
			soapEntity.setUserName(keyword);
			soapEntity.setSsoType(findSSOType(ssoClientInfo));
			soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE);
			UserWebServiceRequest.buildMessage(soapEntity);
			JSONObject permissionInfo = WebServiceHttpClient.get4api(soapEntity);
			JSONArray items = permissionInfo.getJSONArray("Items");
			if (items == null || items.size() <= 0) {
				return res;
			}
			List<UserExtendInfo> list = new ArrayList<>();
			for (int i = 0; i < items.size(); i++) {
				JSONObject object = (JSONObject) items.get(i);
				UserExtendInfo sysUser = new UserExtendInfo();
				sysUser.setUsername(object.getStr("Name"));
				sysUser.setPhone(object.getStr("Mobile"));
				sysUser.setEmail(object.getStr("Email"));
				sysUser.setUserType(object.getStr("UserType"));
				sysUser.setUserCode(object.getStr("Code"));
				sysUser.setUserTypeName(object.getStr("UserTypeName"));
				sysUser.setDeptCode(object.getStr("DeptOrgCode"));
				sysUser.setDeptName(object.getStr("DeptOrgName"));
				list.add(sysUser);
			}
			Integer total = permissionInfo.getInt("Total");
			res.setRecords(list);
			res.setTotal(total);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("分页获取用户信息失败,info={}", e);
		}
		return res;
	}

	private JSONObject getSSOUserInfo(String userCode, String appCode, String appName, String serverToken, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(appCode);
		soapEntity.setAppName(appName);
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);
		soapEntity.setSsoType(findSSOType(ssoClientInfo));

		// 设置一下wsdl的路径
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		processHostAndWsdl(soapEntity, ssoClientInfo);
		// 请求角色
		// 请求权限
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject permissionInfo = WebServiceHttpClient.post(soapEntity);

		return permissionInfo;
	}

	private void processHostAndWsdl(SoapEntity soapEntity, Map ssoClientInfo) {
		String hostUrl = (String) ssoClientInfo.get("ssoHost");
		if (StrUtil.isEmpty(hostUrl)) {
			throw new SSOBusinessException("sso获取信息失败,缺少host");
		}
		if (!hostUrl.startsWith("http")) {
			hostUrl = "http://" + hostUrl;
		}
		soapEntity.setWdslUrl(hostUrl);
		String ipUrls = hostUrl.substring(hostUrl.indexOf("//") + 2);
		String host = null;
		if (ipUrls.contains("/")) {
			host = "http://" + ipUrls.substring(0, ipUrls.indexOf("/"));
		} else host = hostUrl;
		soapEntity.setHost(host);
	}

	private void cacheUserPrivileges(String key, JSONObject privileges) {
		if (CollectionUtils.isEmpty(privileges)) {
			return;
		}
		String jsonStr = JSONUtil.toJsonStr(privileges);
		Cache cache = cacheManager.getCache(CacheConstants.SSO_USER_PRI_INFO);
		cache.put(key, jsonStr);
	}

	protected void cacheUserRoles(String key, JSONObject ssoUserInfos) {
		if (CollectionUtils.isEmpty(ssoUserInfos)) {
			return;
		}
		String jsonStr = JSONUtil.toJsonStr(ssoUserInfos);
		Cache cache = cacheManager.getCache(CacheConstants.SSO_USER_ROLE_INFO);
		cache.put(key, jsonStr);
	}

	private SSOTypeEnum findSSOType(Map ssoClientInfo) {
		Integer type = (Integer) ssoClientInfo.get("type");
		if (type == null || (type.intValue() != 1 && type.intValue() != 2)) {
			type = 2;
		}
		return SSOTypeEnum.parse(type);
	}

	private boolean isAdmin(String serverToken, String sysClass) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		String key = serverToken + "@@" + sysClass;
		if (cache == null || cache.get(key) == null) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		Map map = (Map) cache.get(key).get();
		// 如果是
		log.info("userInfo={}", map);
		return map.containsKey("IsAdmin") && (boolean) map.get("IsAdmin");
	}

	private List<SSORoleInfo> findSSORoleInfoBySoap(String appCode, String appName, String userCode, String sysClass, String serverToken, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(appCode);
		soapEntity.setAppName(appName);
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);
		soapEntity.setSsoType(findSSOType(ssoClientInfo));
		// 设置一下wsdl的路径
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		String wsdlUrl = (String) ssoClientInfo.get(SSOWebServiceConstants.SSO_HOST);
		if (StrUtil.isEmpty(wsdlUrl)) {
			return null;
		}
		processHostAndWsdl(soapEntity, ssoClientInfo);
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject roleInfo = WebServiceHttpClient.post(soapEntity);
		if (roleInfo != null) {
			if (StringUtils.isEmpty(userCode)) {
				throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
			}
		}
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSORoleInfo> roleInfos = roleInfoParse.parse(roleInfo, SSORoleInfo.class, SoapTypeEnum.SOAP_ROLE);
		return roleInfos;
	}

	private List<SSORoleInfo> findSSORoleInfoByHttp(boolean admin, String appCode, String serverToken, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setToken(serverToken);
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		String wsdlUrl = (String) ssoClientInfo.get(SSOWebServiceConstants.SSO_HOST);
		if (StrUtil.isEmpty(wsdlUrl)) {
			return null;
		}
		processHostAndWsdl(soapEntity, ssoClientInfo);
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		String wdslUrl = soapEntity.getWdslUrl();
		wdslUrl += (wdslUrl.endsWith("/") ? "" : "/");
		String url = "";
		if (admin) { // 管理员获取所有的角色信息
			url = wdslUrl + String.format(SSOWebServiceConstants.SSO_API_ROLE_URL_ADMIN, appCode);//"cm/api/AppRole/listbyapp/" + appCode;
		} else { // 否则只获取当前用户的角色信息
			// 获取一下用户id
			url = wdslUrl + SSOWebServiceConstants.SSO_API_USER_INFO_URL;
			Integer userId = findUserId(soapEntity, url);
			url = wdslUrl + String.format(SSOWebServiceConstants.SSO_API_ROLE_URL, userId);//"cm/api/AppRole/listbyuser/" + userId + "/all";
		}
		//UserWebServiceRequest.buildMessage(soapEntity);
		soapEntity.setWdslUrl(url);
		JSONArray roleInfo = WebServiceHttpClient.getToArray(soapEntity);
		List<SSORoleInfo> roleInfos = new ArrayList<>();
		if (roleInfo == null) {
			return roleInfos;
		}
		for (int i = 0; i < roleInfo.size(); i++) {
			SSORoleInfo role = new SSORoleInfo();
			JSONObject cur = (JSONObject) roleInfo.get(i);
			role.setRoleCode((String) cur.get("Code"));
			role.setRoleName((String) cur.get("Name"));
			roleInfos.add(role);
		}
		return roleInfos;
	}

	private Integer findUserId(SoapEntity entity, String url) {
		entity.setWdslUrl(url);
		JSONObject jsonObject = WebServiceHttpClient.get4api(entity);
		if (jsonObject != null && jsonObject.containsKey("UserID")) {
			return jsonObject.getInt("UserID");
		}
		return -1;
	}

	private List<SSOPrivilege> findSSOPriInfoByHttp(boolean admin, String userCode, String appCode, String serverToken, Map ssoClientInfo) {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setToken(serverToken);
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		String wsdlUrl = (String) ssoClientInfo.get(SSOWebServiceConstants.SSO_HOST);
		if (StrUtil.isEmpty(wsdlUrl)) {
			return null;
		}
		// 获取aapid
		Cache cache = cacheManager.getCache(CacheConstants.SSO_APPCODE_ID);
		if (cache == null || cache.get("app") == null) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		Map apps = (Map) cache.get("app").get();
		if (!apps.containsKey(appCode)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		Integer appId = (Integer) apps.get(appCode);
		processHostAndWsdl(soapEntity, ssoClientInfo);
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		String wdslUrl = soapEntity.getWdslUrl();
		wdslUrl += wdslUrl.endsWith("/") ? "" : "/";
		if (admin) {
			wdslUrl += String.format(SSOWebServiceConstants.SSO_API_PRI_URL_ADMIN, appId);//"cm/api/AppPrivilege/all/byapp/" + appId;
		} else {
			wdslUrl += String.format(SSOWebServiceConstants.SSO_API_PRI_URL, userCode, appCode);//"cm/api/AppPrivilege/listbyuser/" + userCode + "?appcode=" + appCode;
		}
		//UserWebServiceRequest.buildMessage(soapEntity);
		soapEntity.setWdslUrl(wdslUrl);
		log.info("获取用户菜单:url ={}", wdslUrl);
		JSONArray roleInfo = WebServiceHttpClient.getToArray(soapEntity);
		List<SSOPrivilege> privileges = new ArrayList<>();
		if (roleInfo == null) {
			return privileges;
		}
		for (int i = 0; i < roleInfo.size(); i++) {
			SSOPrivilege privilege = new SSOPrivilege();
			JSONObject cur = (JSONObject) roleInfo.get(i);
			privilege.setId(cur.getInt("ID"));
			privilege.setParentId(cur.containsKey("Parent_ID") ? cur.getInt("Parent_ID") : -1);
			privilege.setPrivilegeCode(cur.getStr("Code"));
			privilege.setPrivilegeName(cur.getStr("Name"));
			privilege.setSequence(cur.getInt("Sequence"));
			SSOPermissionExtPropertyInfo permissionExtPropertyInfo = new SSOPermissionExtPropertyInfo();
			permissionExtPropertyInfo.setPrivilege_Property_ICON(cur.getStr("Icon"));
			permissionExtPropertyInfo.setPrivilege_Property_URL(cur.getStr("Url"));
			permissionExtPropertyInfo.setPrivilege_Property_PrivilegeType(cur.getStr("PrivilegeType"));
			privilege.setExtPropertyInfo(permissionExtPropertyInfo);
			privileges.add(privilege);
		}
		return privileges;
	}
}
