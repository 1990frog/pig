package com.pig4cloud.pig.admin.sso.common.ssoutil;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.XML;
import com.pig4cloud.pig.admin.sso.common.constants.SSOWebServiceConstants;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @ClassName UserWebServiceResponse
 * @Author Duys
 * @Description 解析返回的xml
 * @Date 2021/12/10 14:34
 **/
public class UserWebServiceResponse {

	public static JSONObject xmlToJson(String xml, SoapTypeEnum type) {
		if (StrUtil.isEmpty(xml)) {
			return null;
		}
		try {
			JSONObject root = XML.toJSONObject(xml);
			if (Objects.isNull(root) || (!root.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE)
					&& !root.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE_1_1))) {
				return null;
			}
			JSONObject envelope = root.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE);
			if (envelope == null) {
				envelope = root.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ENVELOPE_1_1);
			}
			if (Objects.isNull(envelope) || (!envelope.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY)
					&& !envelope.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY_1_1))) {
				return null;
			}
			JSONObject body = envelope.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY);
			if (body == null) {
				body = envelope.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_BODY_1_1);
			}
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
				case SOAP_ALL_ROLE:
					res = parsUserRoleAll(body);
					break;
			}
			return res;
		} catch (Exception e) {
			throw new RuntimeException("解析xml失败");
		}
	}

	public static JSONArray toJsonArrForString(String str) {
		if (StrUtil.isEmpty(str)) {
			return null;
		}
		JSONArray objects = JSONUtil.parseArray(str);
		return objects;
	}

	public static JSONObject toJsonForString(String xml, SoapTypeEnum type) {
		if (StrUtil.isEmpty(xml)) {
			return null;
		}
		try {
			JSONObject root = XML.toJSONObject(xml);
			if (type.equals(SoapTypeEnum.SOAP_USER_PAGE)) {
				JSONObject string = root.getJSONObject("string");
				if (string == null) {
					return null;
				}
				String content = string.getStr("content");
				if (StrUtil.isEmpty(content)) {
					return null;
				}
				String decode = URLDecoder.decode(content, StandardCharsets.UTF_8);
				JSONObject object = JSONUtil.xmlToJson(decode);
				if (object == null) {
					return null;
				}
				return object.getJSONObject("Users");
			}
			if (type.equals(SoapTypeEnum.SOAP_USER_PAGE_TOTAL)) {
				return root.getJSONObject("int");
			}
			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		if (StrUtil.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		String decode = URLDecoder.decode(str, StandardCharsets.UTF_8);
		JSONObject role = XML.toJSONObject(decode);
		return role;
	}

	private static JSONObject parsUserRoleAll(JSONObject json) {
		if (Objects.isNull(json) || !json.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ALL_ROLE)) {
			return null;
		}
		JSONObject roleResponse = json.getJSONObject(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ALL_ROLE);
		if (Objects.isNull(roleResponse) || !roleResponse.containsKey(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ALL_ROLE_RESULT)) {
			return null;
		}
		String str = roleResponse.getStr(SSOWebServiceConstants.WEB_SERVICE_RESPONSE_ALL_ROLE_RESULT);
		if (StrUtil.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		String decode = URLDecoder.decode(str, StandardCharsets.UTF_8);
		JSONObject role = XML.toJSONObject(decode);
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
		if (StrUtil.isEmpty(str)) {
			return null;
		}
		String decode = URLDecoder.decode(str, StandardCharsets.UTF_8);
		// 再来转一次，来获取用户的信息
		JSONObject pri = XML.toJSONObject(decode);
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
		if (StrUtil.isEmpty(str)) {
			return null;
		}
		// 再来转一次，来获取用户的信息
		String decode = URLDecoder.decode(str, StandardCharsets.UTF_8);
		JSONObject org = XML.toJSONObject(decode);
		return org;
	}
}
