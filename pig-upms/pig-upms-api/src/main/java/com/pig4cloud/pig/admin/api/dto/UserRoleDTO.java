package com.pig4cloud.pig.admin.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName UserRoleDTO
 * @Author Duys
 * @Description 用户拥有的角色
 * @Date 2021/7/15 9:51
 **/
@Data
public class UserRoleDTO {
	private Integer userId;
	private List<RoleInfoDTO> roleInfos;
}
