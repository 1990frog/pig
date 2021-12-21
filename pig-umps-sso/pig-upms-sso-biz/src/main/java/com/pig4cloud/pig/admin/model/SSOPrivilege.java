package com.pig4cloud.pig.admin.model;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SSOPrivilege
 * @Author Duys
 * @Description
 * @Date 2021/12/14 14:22
 **/
// SSO的权限，是一个array
@Data
public class SSOPrivilege {
	private Integer sequence;
	private String privilegeName;
	private SSOPermissionExtPropertyInfo extPropertyInfo;
	private String privilegeCode;
	private List<SSOPrivilege> ssoPrivileges;

	// 1.getObject Privileges -> 2.getObject or getArray Privilege
}
