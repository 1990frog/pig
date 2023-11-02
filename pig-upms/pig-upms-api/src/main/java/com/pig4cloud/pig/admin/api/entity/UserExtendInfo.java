package com.pig4cloud.pig.admin.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName UserExtendInfo
 * @Author Duys
 * @Date 2023/11/2 17:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserExtendInfo extends SysUser {
	private String deptName;
	private String deptCode;
	private String userType;
	private String userTypeName;
	private String userCode;
}
