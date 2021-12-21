package com.pig4cloud.pig.admin.model;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SSOPermissionInfo
 * @Author Duys
 * @Description 权限信息
 * @Date 2021/12/14 10:41
 **/
@Data
public class SSOPermissionInfo {
	private String privilegeCode;
	private String privilegeName;
	private Integer sequence;
	private SSOPermissionExtPropertyInfo extPropertyInfo;
	// 嵌套的
	private List<SSOPermissionInfo> childInfos;
}
