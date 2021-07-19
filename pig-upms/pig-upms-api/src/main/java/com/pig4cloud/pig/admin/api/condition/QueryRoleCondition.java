package com.pig4cloud.pig.admin.api.condition;

import lombok.Data;

import java.util.List;

/**
 * @ClassName QueryRoleCondition
 * @Author Duys
 * @Description 内部查询参数
 * @Date 2021/7/14 18:01
 **/
@Data
public class QueryRoleCondition extends BaseCondition {

	/**
	 * 系统编码
	 */
	private String sysClass;

	/**
	 * 角色名称
	 */
	private String roleName;

	/**
	 * 用户ids
	 */
	private List<Integer> userIds;

	/**
	 * 角色ids
	 */
	private List<Integer> roleIds;

	/**
	 * 角色id
	 */
	private Integer roleId;

	/**
	 * 菜单id
	 */
	private List<Integer> menuIds;
}
