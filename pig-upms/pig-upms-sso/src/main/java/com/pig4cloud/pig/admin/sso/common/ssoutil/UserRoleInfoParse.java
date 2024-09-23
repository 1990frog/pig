package com.pig4cloud.pig.admin.sso.common.ssoutil;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.sso.model.SSOPermissionExtPropertyInfo;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName UserRoleInfoPars
 * @Author Duys
 * @Description
 * @Date 2021/12/14 10:49
 **/
public class UserRoleInfoParse {
	private volatile static UserRoleInfoParse INSTANCE;

	public static UserRoleInfoParse getInstance() {
		if (INSTANCE == null) {
			synchronized (UserRoleInfoParse.class) {
				if (INSTANCE == null) {
					INSTANCE = new UserRoleInfoParse();
				}
			}
		}
		return INSTANCE;
	}

	private UserRoleInfoParse() {

	}

	public <T> List<T> parse(JSONObject source, Class<T> t, SoapTypeEnum soapTypeEnum) {
		List<T> res = new ArrayList<>();
		// 拿到
		switch (soapTypeEnum) {
			case SOAP_ALL_ROLE:
			case SOAP_ROLE:
				parseByRole(source, t, res);
				break;
			case SOAP_PER:
				parseByPermission(source, t, res);
				break;
			case SOAP_ORG:
				break;
		}
		return res;
	}

	// 宽度优先遍历，按层遍历
	public void parseSSOMenu(JSONObject source, List<SSOPrivilege> ans) {
		// 1. 解析 Privileges
		if (Objects.isNull(source) || !source.containsKey("Privileges")) {
			return;
		}
		JSONObject privileges = source.getJSONObject("Privileges");
		if (Objects.isNull(privileges) || !privileges.containsKey("Privilege")) {
			return;
		}
		// 2.解析 Privilege 可能是一个object，可能是一个array
		try {
			// 这一层的
			JSONArray privilegeArrays = privileges.getJSONArray("Privilege");
			if (Objects.isNull(privilegeArrays) || privilegeArrays.size() <= 0) {
				return;
			}
			// 这一层的遍历
			for (int i = 0; i < privilegeArrays.size(); i++) {
				JSONObject privilege = (JSONObject) privilegeArrays.get(i);
				if (Objects.isNull(privilege)) {
					continue;
				}
				// 解析每一个Privilege参数
				SSOPrivilege res = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(privilege), SSOPrivilege.class);
				if (privilege.containsKey("ExtPropertyValues")) {
					JSONObject extPropertyValues = privilege.getJSONObject("ExtPropertyValues");
					SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
					res.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
				}
				// 找子类
				List<SSOPrivilege> childs = processChild(privilege);
				res.setSsoPrivileges(childs);
				ans.add(res);
			}
		} catch (ClassCastException classCastException) {
			JSONObject privilegeObject = privileges.getJSONObject("Privilege");
			try {
				SSOPrivilege resObj = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(privilegeObject), SSOPrivilege.class);
				if (privilegeObject.containsKey("ExtPropertyValues")) {
					JSONObject extPropertyValues = privilegeObject.getJSONObject("ExtPropertyValues");
					SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
					resObj.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
					List<SSOPrivilege> childs = processChild(privilegeObject);
					resObj.setSsoPrivileges(childs);
					ans.add(resObj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<SSOPrivilege> processChild(JSONObject privilege) {
		List<SSOPrivilege> res = new ArrayList<>();
		// 1. 解析 Privileges
		if (Objects.isNull(privilege) || !privilege.containsKey("Privileges")) {
			return res;
		}
		JSONObject privileges = privilege.getJSONObject("Privileges");
		if (Objects.isNull(privileges) || !privileges.containsKey("Privilege")) {
			return res;
		}
		// 这儿也有一层
		try {
			JSONArray privilegeArrays = privileges.getJSONArray("Privilege");
			if (Objects.isNull(privilegeArrays) || privilegeArrays.size() <= 0) {
				return res;
			}
			// 这一层的遍历
			Iterator<Object> iterator = privilegeArrays.stream().iterator();
			if (Objects.isNull(iterator)) {
				return res;
			}
			while (iterator.hasNext()) {
				JSONObject next = (JSONObject) iterator.next();
				if (Objects.isNull(next)) {
					continue;
				}
				// 解析每一个Privilege参数
				SSOPrivilege resObj = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(next), SSOPrivilege.class);
				if (next.containsKey("ExtPropertyValues")) {
					JSONObject extPropertyValues = next.getJSONObject("ExtPropertyValues");
					SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
					resObj.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
				}
				List<SSOPrivilege> ssoPrivileges = processChild(next);
				resObj.setSsoPrivileges(ssoPrivileges);
				res.add(resObj);
			}
		} catch (ClassCastException classCastException) {
			try {
				JSONObject privilegeObject = privileges.getJSONObject("Privilege");
				SSOPrivilege resObj = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(privilegeObject), SSOPrivilege.class);
				if (privilegeObject.containsKey("ExtPropertyValues")) {
					JSONObject extPropertyValues = privilegeObject.getJSONObject("ExtPropertyValues");
					SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo =
							com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
					resObj.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
					List<SSOPrivilege> childs = processChild(privilegeObject);
					resObj.setSsoPrivileges(childs);
					res.add(resObj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}


	private <T> void parseByPermission(JSONObject source, Class<T> clazz, List<T> ans) {
		// 1. 解析 Privileges
		if (Objects.isNull(source) || !source.containsKey("Privileges")) {
			return;
		}
		JSONObject privileges = source.getJSONObject("Privileges");
		if (Objects.isNull(privileges) || !privileges.containsKey("Privilege")) {
			return;
		}
		// 2.解析 Privilege 可能是一个object，可能是一个array
		try {
			JSONArray privilegeArrays = privileges.getJSONArray("Privilege");
			if (Objects.isNull(privilegeArrays) || privilegeArrays.size() <= 0) {
				return;
			}
			Iterator<Object> iterator = privilegeArrays.stream().iterator();
			if (Objects.isNull(iterator)) {
				return;
			}
			while (iterator.hasNext()) {
				JSONObject next = (JSONObject) iterator.next();
				if (Objects.isNull(next)) {
					continue;
				}
				// 解析每一个Privilege参数
				SSOPrivilege res = (SSOPrivilege) com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(next), clazz);
				if (next.containsKey("ExtPropertyValues")) {
					JSONObject extPropertyValues = next.getJSONObject("ExtPropertyValues");
					SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo =
							com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
					res.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
				}
				ans.add((T) res);
				if (next.containsKey("Privileges")) {
					parseByPermission(next, clazz, ans);
				}
			}
		} catch (ClassCastException classCastException) {
			JSONObject privilegeObject = privileges.getJSONObject("Privilege");
			SSOPrivilege res = (SSOPrivilege) com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(privilegeObject), clazz);
			if (privilegeObject.containsKey("ExtPropertyValues")) {
				JSONObject extPropertyValues = privilegeObject.getJSONObject("ExtPropertyValues");
				SSOPermissionExtPropertyInfo ssoPermissionExtPropertyInfo =
						com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(extPropertyValues), SSOPermissionExtPropertyInfo.class);
				res.setExtPropertyInfo(ssoPermissionExtPropertyInfo);
			}
			ans.add((T) res);
			if (privilegeObject.containsKey("Privileges")) {
				parseByPermission(privilegeObject, clazz, ans);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private <T> void parseByRole(JSONObject source, Class<T> t, List<T> ans) {
		// 解析
		if (Objects.isNull(source) || !source.containsKey("AppRoles")) {
			return;
		}
		JSONObject appRoles = source.getJSONObject("AppRoles");
		if (Objects.isNull(appRoles) || !appRoles.containsKey("AppRole")) {
			return;
		}
		try {
			// 走jsonArray的路线解析
			JSONArray appRoleArray = appRoles.getJSONArray("AppRole");
			if (Objects.isNull(appRoleArray) || appRoleArray.size() <= 0) {
				return;
			}
			Iterator<Object> iterator = appRoleArray.stream().iterator();
			if (Objects.isNull(iterator)) {
				return;
			}
			while (iterator.hasNext()) {
				JSONObject appRole = (JSONObject) iterator.next();
				T res = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(appRole), t);
				ans.add(res);
			}
		} catch (ClassCastException classCastException) {
			// 走jsonObject的路线解析
			JSONObject appRole = appRoles.getJSONObject("AppRole");
			if (Objects.isNull(appRole)) {
				return;
			}
			T res = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(appRole), t);
			ans.add(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*private <T> T processString2Object(String context, Class<T> clazz) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String string = objectMapper.writeValueAsString(context);
			return objectMapper.readValue(string, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("类型转换异常 e ={} ", e);
		}
	}*/
}
