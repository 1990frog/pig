package com.pig4cloud.pig.admin.sso.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserRoleInfoParse;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.sso.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.model.SSORoleInfo;
import com.pig4cloud.pig.admin.sso.model.SoapEntity;
import com.pig4cloud.pig.admin.sso.service.IRemoteService;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName RemoteServiceImpl
 * @Author Duys
 * @Description
 * @Date 2021/12/9 10:42
 **/
@Service
public class RemoteServiceImpl implements IRemoteService {


	@Override
	public List<SSORoleInfo> getSSORoleInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String username = serverInfoMap.get("username");
		if (StrUtil.isEmpty(username)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		String userCode = username.split("@@")[0];
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(serverInfoMap.get("appCode"));
		soapEntity.setAppName(serverInfoMap.get("appName"));
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);

		// 设置一下wsdl的路径
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		String wsdlUrl = (String) ssoClientInfo.get("ssoHost");
		if (StrUtil.isEmpty(wsdlUrl)) {
			return null;
		}
		if (!wsdlUrl.startsWith("http")) {
			wsdlUrl = "http://" + wsdlUrl;
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
	 * @param serverToken   sso 服务端token
	 * @param serverInfoMap sso 登录的时候使用的参数
	 * @param ssoClientInfo sso 配置信息，在getaway上
	 * @return 本地的用户
	 */
	@Override
	public List<SSOPrivilege> getSSOPrivilege(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		JSONObject permissionInfo = getSSOUserInfo(serverToken, serverInfoMap, ssoClientInfo);
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> privileges = roleInfoParse.parse(permissionInfo, SSOPrivilege.class, SoapTypeEnum.SOAP_PER);
		return privileges;
	}

	@Override
	public List<SSOPrivilege> getSSOMenus(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		JSONObject permissionInfo = getSSOUserInfo(serverToken, serverInfoMap, ssoClientInfo);
		// 这里需要按照另外的解析方式。。。
		UserRoleInfoParse roleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> privileges = new ArrayList<>();
		// 一层一层的去解析
		roleInfoParse.parseSSOMenu(permissionInfo, privileges);
		return privileges;
	}

	private JSONObject getSSOUserInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo) {
		String username = serverInfoMap.get("username");
		if (StrUtil.isEmpty(username)) {
			throw new RuntimeException("用户信息为空，请重新登录");
		}
		String userCode = username.split("@@")[0];
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode(serverInfoMap.get("appCode"));
		soapEntity.setAppName(serverInfoMap.get("appName"));
		soapEntity.setUserCode(userCode);
		soapEntity.setToken(serverToken);

		// 设置一下wsdl的路径
		//String url = (String) ssoClientInfo.get("serverUrl");
		//String wsdlUrl = getWsdlUrl(url);
		String wsdlUrl = (String) ssoClientInfo.get("ssoHost");
		if (StrUtil.isEmpty(wsdlUrl)) {
			return null;
		}
		if (!wsdlUrl.startsWith("http")) {
			wsdlUrl = "http://" + wsdlUrl;
		}
		soapEntity.setHost(wsdlUrl);
		// 请求角色
		// 请求权限
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject permissionInfo = WebServiceHttpClient.post(soapEntity);
		return permissionInfo;
	}

	private String getWsdlUrl(String serverUrl) {
		String wsdlUrl = null;
		try {
			URL url = new URL(serverUrl);
			String host = url.getHost();
			int port = url.getPort();
			if (host.contains("http://")) {
				wsdlUrl = host + ":" + port;
			} else {
				wsdlUrl = "http://" + host + ":" + port;
			}
			return wsdlUrl;
		} catch (Exception e) {
			throw new SSOBusinessException(ResponseCodeEnum.SYSTEM_ERROR);
		}
	}


	public static void main(String[] args) throws Exception {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setHost("http://192.168.0.147:9011");
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("数据质量核查与分析软件");
		soapEntity.setAppCode("DATA_QUALIT");
		soapEntity.setToken("1d0c9ef8-46ea-44e6-81a4-733628300041");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject post = WebServiceHttpClient.get(soapEntity);
		//System.out.println();
		//System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(post));
		UserRoleInfoParse userRoleInfoParse = UserRoleInfoParse.getInstance();
		List<SSOPrivilege> ans = new ArrayList<>();
		userRoleInfoParse.parseSSOMenu(post, ans);
		//System.out.println("ans -> " + com.alibaba.fastjson.JSONObject.toJSONString(ans));
		//SysMenuServiceImpl sysMenuService = new SysMenuServiceImpl();
	/*	for (SSOPrivilege li : list) {
			System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(li));
			System.out.println();
		}*/
		List<MenuTree> menuTrees = new ArrayList<>();
		//sysMenuService.processMenu(ans, menuTrees);
		//System.out.println();
		for (MenuTree li : menuTrees) {
			//System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(li));
			//System.out.println();
		}

		//角色 {"AppRoles":{"AppRole":{"RoleCode":"Admin","RoleName":"管理员角色"}}}
		//    {"AppRoles":{"AppRole":[{"RoleCode":"Admin","RoleName":"管理员角色"},{"RoleCode":"EveryOne","RoleName":"所有人角色"}]}}

	}

}
