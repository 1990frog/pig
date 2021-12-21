package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.json.JSONObject;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.common.ssoutil.UserRoleInfoParse;
import com.pig4cloud.pig.admin.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.model.SSOPrivilege;
import com.pig4cloud.pig.admin.model.SSORoleInfo;
import com.pig4cloud.pig.admin.model.SoapEntity;
import com.pig4cloud.pig.admin.service.IRemoteService;
import org.springframework.stereotype.Service;

import java.net.URL;
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
		String wsdlUrl = getWsdlUrl(url);
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
		String wsdlUrl = getWsdlUrl(url);
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
		soapEntity.setType(SoapTypeEnum.SOAP_ORG);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("授权管理系统");
		soapEntity.setAppCode("Centralism");
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject post = WebServiceHttpClient.post(soapEntity);
		System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(post));

		//角色 {"AppRoles":{"AppRole":{"RoleCode":"Admin","RoleName":"管理员角色"}}}
		//    {"AppRoles":{"AppRole":[{"RoleCode":"Admin","RoleName":"管理员角色"},{"RoleCode":"EveryOne","RoleName":"所有人角色"}]}}

	}

}
