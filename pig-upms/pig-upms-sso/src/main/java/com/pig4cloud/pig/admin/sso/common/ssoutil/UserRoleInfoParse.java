package com.pig4cloud.pig.admin.sso.common.ssoutil;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
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

	public static void main(String[] args) {
		UserRoleInfoParse parse = new UserRoleInfoParse();
		JSONObject obj = new JSONObject("{\n" +
				"\t\"Privileges\": {\n" +
				"\t\t\"Privilege\": [\n" +
				"\t\t\t{\n" +
				"\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\"PrivilegeName\": \"应用系统管理模块\",\n" +
				"\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t},\n" +
				"\t\t\t\t\"PrivilegeCode\": \"AppManageModule\",\n" +
				"\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 400,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"应用系统管理\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"AppManage\",\n" +
				"\t\t\t\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看应用系统\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewApp\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 110,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"新增应用系统\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Button\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"new.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"AddApp\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 120,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"修改应用系统\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Button\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"modify.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateApp\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 130,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"设置应用系统服务\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageWidht\": \"600\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Button\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageHeight\": \"420\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../VectorModule/SetAppService.aspx?AppID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"SetAppService\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 140,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看应用系统审计\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageWidht\": \"700\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageHeight\": \"480\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/SingleAuditList.aspx?ObjectID={0}&ObjectType=App\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewAppSingleAudit\"\n" +
				"\t\t\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t\t\t]\n" +
				"\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 1100,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"字典管理\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"DictionaryManage\",\n" +
				"\t\t\t\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看字典\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"DictionaryView.aspx?AppID={1}&ID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewDictionary\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 110,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"新增字典\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"new.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"DictionaryAdd.aspx?AppID={1}&ParentID={2}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"AddDictionary\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 120,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"修改字典\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"modify.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"DictionaryUpdate.aspx?AppID={1}&ID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateDictionary\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 130,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"删除字典\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"delete.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"DeleteDictionary\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 140,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"修改字典排序号\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"order.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateDictionarySequence\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 150,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看字典审计\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageWidht\": \"700\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageHeight\": \"480\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/SingleAuditList.aspx?ObjectID={0}&ObjectType=Dictionary\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewDictionarySingleAudit\"\n" +
				"\t\t\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t\t\t]\n" +
				"\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 1200,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"扩展属性管理\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"AppExtPropertyManage\",\n" +
				"\t\t\t\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"AppExtPropertyView.aspx?ID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewAppExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"新增扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"new.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"AppExtPropertyAdd.aspx?AppID={1}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"AddAppExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 300,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"修改扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"modify.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"AppExtPropertyUpdate.aspx?ID={0}&AppID={1}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateAppExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 400,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"删除扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"delete.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"DeleteAppExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 500,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"更新扩展属性排序号\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"order.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateAppExtPropertySequence\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 600,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看扩展属性审计\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageWidht\": \"700\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageHeight\": \"480\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/SingleAuditList.aspx?ObjectID={0}&ObjectType=AppExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewAppExtPropertySingleAudit\"\n" +
				"\t\t\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t\t\t]\n" +
				"\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t]\n" +
				"\t\t\t\t}\n" +
				"\t\t\t},\n" +
				"\t\t\t{\n" +
				"\t\t\t\t\"Sequence\": 300,\n" +
				"\t\t\t\t\"PrivilegeName\": \"消息管理模块\",\n" +
				"\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t},\n" +
				"\t\t\t\t\"PrivilegeCode\": \"MessageModule\",\n" +
				"\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"特定消息\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../MessageModule/SysMessageList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"MessageManage\"\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"通知公告\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../MessageModule/NoticeList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"NoticeManage\"\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t]\n" +
				"\t\t\t\t}\n" +
				"\t\t\t},\n" +
				"\t\t\t{\n" +
				"\t\t\t\t\"Sequence\": 400,\n" +
				"\t\t\t\t\"PrivilegeName\": \"日志审计模块\",\n" +
				"\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t},\n" +
				"\t\t\t\t\"PrivilegeCode\": \"AudtiManageModule\",\n" +
				"\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"审计事件查询\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/AuditEventList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"AuditEventQuery\"\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"账号登录记录\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/AccountOperEventList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"AccountOperateQuery\"\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"系统日志查询\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/SysLogEventList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"SysLogQuery\"\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t]\n" +
				"\t\t\t\t}\n" +
				"\t\t\t},\n" +
				"\t\t\t{\n" +
				"\t\t\t\t\"Sequence\": 550,\n" +
				"\t\t\t\t\"PrivilegeName\": \"系统设置模块\",\n" +
				"\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t},\n" +
				"\t\t\t\t\"PrivilegeCode\": \"SystemManageModule\",\n" +
				"\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"扩展属性管理\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../ExtPropertyModule/ExtPropertyList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"ExtPropertyManage\",\n" +
				"\t\t\t\t\t\t\t\"Privileges\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege\": [\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"ExtPropertyView.aspx?ID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 200,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"新增扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"new.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"ExtPropertyAdd.aspx\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"AddExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 300,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"修改扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"modify.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"ExtPropertyUpdate.aspx?ID={0}\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 400,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"删除扩展属性\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"delete.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"DeleteExtProperty\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 500,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"更新扩展属性排序号\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"order.gif\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"UpdateExtPropertySequence\"\n" +
				"\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\t\t\t\"Sequence\": 600,\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeName\": \"查看扩展属性审计\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageWidht\": \"700\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_ICON\": \"view.gif\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"PageHeight\": \"480\",\n" +
				"\t\t\t\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../AuditModule/SingleAuditList.aspx?ObjectID={0}&ObjectType=User\"\n" +
				"\t\t\t\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\t\t\t\"PrivilegeCode\": \"ViewExtPropertySingleAudit\"\n" +
				"\t\t\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t\t\t]\n" +
				"\t\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t{\n" +
				"\t\t\t\t\t\t\t\"Sequence\": 100,\n" +
				"\t\t\t\t\t\t\t\"PrivilegeName\": \"系统初始化\",\n" +
				"\t\t\t\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\",\n" +
				"\t\t\t\t\t\t\t\t\"Privilege_Property_URL\": \"../SystemInitModule/SystemInitList.aspx\"\n" +
				"\t\t\t\t\t\t\t},\n" +
				"\t\t\t\t\t\t\t\"PrivilegeCode\": \"SystemInitManage\"\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t]\n" +
				"\t\t\t\t}\n" +
				"\t\t\t},\n" +
				"\t\t\t{\n" +
				"\t\t\t\t\"Sequence\": 600,\n" +
				"\t\t\t\t\"PrivilegeName\": \"密码管理模块\",\n" +
				"\t\t\t\t\"ExtPropertyValues\": {\n" +
				"\t\t\t\t\t\"Privilege_Property_PrivilegeType\": \"Menu\"\n" +
				"\t\t\t\t},\n" +
				"\t\t\t\t\"PrivilegeCode\": \"PasswordManageModule\"\n" +
				"\t\t\t}\n" +
				"\t\t]\n" +
				"\t}\n" +
				"}");
		/*List<SSOPrivilege> list = new ArrayList<>();
		parse.parseByPermission(obj, SSOPrivilege.class, list);
		for (SSOPrivilege info : list) {
			System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(info));
		}*/
		List<SSOPrivilege> list = new ArrayList<>();
		//SysMenuServiceImpl sysMenuService = new SysMenuServiceImpl();
		parse.parseSSOMenu(obj, list);
	/*	for (SSOPrivilege li : list) {
			System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(li));
			System.out.println();
		}*/
		List<MenuTree> menuTrees = new ArrayList<>();
		//sysMenuService.processMenu(list, menuTrees);
		for (MenuTree li : menuTrees) {
			//System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(li));
			System.out.println();
		}
		/*SSORoleInfo ssoRoleInfo =;
		System.out.println(ssoRoleInfo.getRoleCode());
		System.out.println(ssoRoleInfo.getRoleName());*/
	}
}
