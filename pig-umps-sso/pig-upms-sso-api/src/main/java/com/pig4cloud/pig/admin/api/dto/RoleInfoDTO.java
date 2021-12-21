package com.pig4cloud.pig.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName RoleInfoDTO
 * @Author Duys
 * @Description
 * @Date 2021/7/15 10:24
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleInfoDTO {
	private Integer roleId;
	private String roleName;
}