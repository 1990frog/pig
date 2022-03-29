package com.pig4cloud.pig.admin.common.constants;

/**
 * @ClassName SSOWebServiceConstants
 * @Author Duys
 * @Description sso webService需要的一些变量
 * @Date 2021/12/10 14:17
 **/
public interface SSOWebServiceConstants {

	// target name-space
	String WEB_SERVICE_NAMESPACE = "http://Centralism.WebService/";

	// Prefix
	String WEB_SERVICE_PREFIX = "soap";
	String WEB_SERVICE_REQUEST_HEADER = "CentralismSoapHeader";

	//

	String WEB_SERVICE_RESPONSE_ENVELOPE = "soap:Envelope";
	String WEB_SERVICE_RESPONSE_BODY = "soap:Body";

	// 获取用户角色
	String WEB_SERVICE_USER_ROLE = "/cws/AppRoleWebService.asmx?wdsl";
	String WEB_SERVICE_USER_ROLE_REQUEST = "GetUserRoles";
	String WEB_SERVICE_RESPONSE_ROLE = "GetUserRolesResponse";
	String WEB_SERVICE_RESPONSE_ROLE_RESULT = "GetUserRolesResult";

	// 获取用户权限
	String WEB_SERVICE_USER_PRIVILEGE = "/cws/PrivilegeWebService.asmx?wdsl";
	String WEB_SERVICE_USER_PRIVILEGE_REQUEST = "GetUserAccessAllPrivileges";
	String WEB_SERVICE_RESPONSE_PRIVILEGE = "GetUserAccessAllPrivilegesResponse";
	String WEB_SERVICE_RESPONSE_PRIVILEGE_RESULT = "GetUserAccessAllPrivilegesResult";

	// 获取用户组织
	String WEB_SERVICE_USER_ORG = "/cws/OrgWebService.asmx?wdsl";
	String WEB_SERVICE_USER_ORG_REQUEST = "GetUserOrgs";
	String WEB_SERVICE_RESPONSE_ORG = "GetUserOrgsResponse";
	String WEB_SERVICE_RESPONSE_ORG_RESULT = "GetUserOrgsResult";

}
