package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.condition.QueryRoleCondition;
import com.pig4cloud.pig.admin.api.dto.MenuTreeDTO;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserRoleDTO;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.api.entity.SysUser;

import java.util.List;

/**
 * @ClassName SysInnerService
 * @Author Duys
 * @Description
 * @Date 2021/7/15 10:03
 **/
public interface SysInnerService {


	/**
	 * 内部使用
	 *
	 * @return
	 */
	Page<SysRole> pageByParam(QueryRoleCondition condition);


	/**
	 * 内部使用-获取用户角色信息
	 *
	 * @param condition
	 * @return
	 */
	List<UserRoleDTO> getUserRoleByUserId(QueryRoleCondition condition);

	/**
	 * 内部使用-获取角色的菜单信息
	 *
	 * @param condition
	 * @return
	 */
	List<MenuTreeDTO> getRoleMenus(QueryRoleCondition condition);

	/**
	 * 内部使用，更新菜单和角色关系
	 *
	 * @param condition
	 * @return
	 */
	Boolean edit(QueryRoleCondition condition);

	/**
	 * 内部使用，新增用户
	 *
	 * @param userDTO
	 * @return
	 */
	Integer addUser(UserDTO userDTO);

	/**
	 * 内部调用，更新用户
	 *
	 * @param userDTO
	 * @return
	 */
	Boolean updateUser(UserDTO userDTO);

	/**
	 * 内部调用，获取用户的角色信息
	 *
	 * @param userIds
	 * @return
	 */
	List<UserRoleDTO> findUserRoleInfo(List<Integer> userIds);

	/**
	 * 内部删除用户
	 *
	 * @param userId
	 * @return
	 */
	Boolean deleteUserByUserId(Integer userId);

	/**
	 * 内部使用
	 */
	SysRole addRole(SysRole sysRole);

	/**
	 * 内部使用
	 */
	Boolean updateRole(SysRole sysRole);

	/**
	 * 内部使用
	 */
	Boolean deleteRole(SysRole sysRole);

	/**
	 * 内部获取用户
	 */
	SysUser findUserInfoByToken(String token);
}
