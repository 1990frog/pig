package com.pig4cloud.pig.admin.model;

import lombok.Data;

/**
 * @ClassName SSOPermissionExtPropertyInfo
 * @Author Duys
 * @Description
 * @Date 2021/12/14 10:45
 **/
@Data
public class SSOPermissionExtPropertyInfo {
	// Privilege_Property_PrivilegeType
	private String privilege_Property_PrivilegeType;
	// Privilege_Property_ICON
	private String privilege_Property_ICON;
	// Privilege_Property_URL
	private String privilege_Property_URL;
	private String pageWidht;
	private String pageHeight;
}
