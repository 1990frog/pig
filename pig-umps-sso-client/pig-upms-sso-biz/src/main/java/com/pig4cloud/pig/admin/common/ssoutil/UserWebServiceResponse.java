package com.pig4cloud.pig.admin.common.ssoutil;

import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.common.constants.SSOWebServiceConstants;
import com.pig4cloud.pig.admin.common.enums.SoapTypeEnum;

import java.util.Objects;

/**
 * @ClassName UserWebServiceResponse
 * @Author Duys
 * @Description 解析返回的xml
 * @Date 2021/12/10 14:34
 **/
public class UserWebServiceResponse {

	public static JSONObject xmlToJson(String xml, SoapTypeEnum type) {
		if (StringUtils.isEmpty(xml)) {
			return null;
		}
		try {
			JSONObject root = XML.toJSONObject(xml);
			if (Objects.isNull(root) || !root.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE)) {
				return null;
			}
			JSONObject envelope = root.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE);
			if (Objects.isNull(envelope) || !envelope.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY)) {
				return null;
			}
			JSONObject body = envelope.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY);
			JSONObject res = null;
			switch (type) {
				// 角色
				case SOAP_ROLE:
					res = parsUserRole(body);
					break;
				// 权限
				case SOAP_PER:
					res = parsUserPri(body);
					break;
				// 组织
				case SOAP_ORG:
					res = parsUserOrg(body);
					break;
			}
			return res;
		} catch (Exception e) {
			throw new RuntimeException("解析xml失败");
		}
	}

	private static JSONObject parsUserRole(JSONObject json) {
		if (Objects.isNull(json) || !json.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ROLE)) {
			return null;
		}
		JSONObject roleResponse = json.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ROLE);
		if (Objects.isNull(roleResponse) || !roleResponse.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ROLE_RESULT)) {
			return null;
		}
		String str = roleResponse.getStr(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ROLE_RESULT);
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		JSONObject role = XML.toJSONObject(str);
		return role;
	}

	private static JSONObject parsUserPri(JSONObject json) {
		if (Objects.isNull(json) || !json.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_PRIVILEGE)) {
			return null;
		}
		JSONObject roleResponse = json.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_PRIVILEGE);
		if (Objects.isNull(roleResponse) || !roleResponse.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_PRIVILEGE_RESULT)) {
			return null;
		}
		String str = roleResponse.getStr(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_PRIVILEGE_RESULT);
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		JSONObject pri = XML.toJSONObject(str);
		return pri;
	}

	private static JSONObject parsUserOrg(JSONObject json) {
		if (Objects.isNull(json) || !json.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ORG)) {
			return null;
		}
		JSONObject roleResponse = json.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ORG);
		if (Objects.isNull(roleResponse) || !roleResponse.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ORG_RESULT)) {
			return null;
		}
		String str = roleResponse.getStr(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ORG_RESULT);
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		JSONObject org = XML.toJSONObject(str);
		return org;
	}
}
