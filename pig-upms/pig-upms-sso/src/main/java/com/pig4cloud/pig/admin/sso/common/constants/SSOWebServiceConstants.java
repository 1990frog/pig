package com.pig4cloud.pig.admin.sso.common.constants;

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
	String WEB_SERVICE_RESPONSE_ENVELOPE_1_1 = "s:Envelope";
	String WEB_SERVICE_RESPONSE_BODY = "soap:Body";

	String WEB_SERVICE_RESPONSE_BODY_1_1 = "s:Body";

	// 获取用户角色
	String WEB_SERVICE_USER_ROLE = "/AppRoleWebService.asmx";
	String WEB_SERVICE_USER_ROLE_REQUEST = "GetUserRoles";
	String WEB_SERVICE_RESPONSE_ROLE = "GetUserRolesResponse";
	String WEB_SERVICE_RESPONSE_ROLE_RESULT = "GetUserRolesResult";

	// 获取所有的角色信息
	String WEB_SERVICE_USER_ALL_ROLE_REQUEST = "GetAllRoles";
	String WEB_SERVICE_RESPONSE_ALL_ROLE = "GetAllRolesResponse";
	String WEB_SERVICE_RESPONSE_ALL_ROLE_RESULT = "GetAllRolesResult";
	// 获取用户权限
	String WEB_SERVICE_USER_PRIVILEGE = "/PrivilegeWebService.asmx";
	String WEB_SERVICE_USER_PRIVILEGE_REQUEST = "GetUserAccessAllPrivileges";
	String WEB_SERVICE_RESPONSE_PRIVILEGE = "GetUserAccessAllPrivilegesResponse";
	String WEB_SERVICE_RESPONSE_PRIVILEGE_RESULT = "GetUserAccessAllPrivilegesResult";

	// 获取用户组织
	String WEB_SERVICE_USER_ORG = "/OrgWebService.asmx";
	String WEB_SERVICE_USER_ORG_REQUEST = "GetUserOrgs";
	String WEB_SERVICE_RESPONSE_ORG = "GetUserOrgsResponse";
	String WEB_SERVICE_RESPONSE_ORG_RESULT = "GetUserOrgsResult";


	// 获取用户组织
	String WEB_SERVICE_USER_PAGE = "/UserWebService.asmx/QueryUsersByPager";
	String WEB_SERVICE_USER_PAGE_TOTAL = "/UserWebService.asmx/QueryUserCount";

	String WEB_SERVICE_USER_PAGE_1_1 = "/cm/api/User/paged/v2";

	String SSO_HOST = "ssoHost";
	String SSO_API_PRI_URL = "cm/api/AppPrivilege/listbyuser/%s?appcode=%s";
	String SSO_API_PRI_URL_ADMIN = "cm/api/AppPrivilege/all/byapp/%s";
	String SSO_API_ROLE_URL = "cm/api/AppRole/listbyuser/%s/all";
	String SSO_API_ROLE_URL_ADMIN = "cm/api/AppRole/listbyapp/%s";

	String SSO_API_USER_INFO_URL = "cm/api/User/current";

	String ROLE_ALL = "ROLE_ALL";
	String ROLE_CURRENT = "ROLE_CURRENT";
}
