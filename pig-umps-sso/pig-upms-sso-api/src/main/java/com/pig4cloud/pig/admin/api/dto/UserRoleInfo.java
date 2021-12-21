package com.pig4cloud.pig.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName UserRoleInfo
 * @Author Duys
 * @Description
 * @Date 2021/7/19 10:48
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleInfo {
	private Integer userId;
	private Integer roleId;
	private String roleName;
}
