package com.pig4cloud.pig.admin.controller.inner;

import com.pig4cloud.pig.admin.api.condition.QueryRoleCondition;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.service.SysInnerService;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @ClassName InnerController
 * @Author Duys
 * @Description 内部接口
 * @Date 2021/7/14 17:46
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/inner")
@Api(value = "inner-内部接口", tags = "inner-内部接口")
public class InnerController {
	private final SysInnerService sysInnerService;

	// 批量获取角色信息
	@Inner
	@GetMapping(path = "/role/page")
	public R getRolePage(QueryRoleCondition condition) {
		return R.ok(sysInnerService.pageByParam(condition));
	}

	// 批量获取用户信息
	@Inner
	@GetMapping(path = "/user/role")
	public R getUserRole(QueryRoleCondition condition) {
		return R.ok(sysInnerService.getUserRoleByUserId(condition));
	}

	// 获取当前角色所有的菜单
	@Inner
	@GetMapping(path = "/role/menu")
	public R getRoleMenus(QueryRoleCondition condition) {
		return R.ok(sysInnerService.getRoleMenus(condition));
	}

	// 编辑菜单信息的角色
	@Inner
	@PutMapping(path = "/role/menu/edit")
	public R edit(@RequestBody QueryRoleCondition condition) {
		return R.ok(sysInnerService.edit(condition));
	}

	// 新增用户
	@Inner
	@PostMapping(path = "/user/add")
	public R addUser(@RequestBody UserDTO userDTO) {
		return R.ok(sysInnerService.addUser(userDTO));
	}

	// 新增用户
	@Inner
	@PostMapping(path = "/user/update")
	public R updateUser(@RequestBody UserDTO userDTO) {
		return R.ok(sysInnerService.updateUser(userDTO));
	}

	@Inner
	@PostMapping(path = "/user/roleInfo")
	public R findUserRoleInfo(@RequestBody QueryRoleCondition condition) {
		return R.ok(sysInnerService.findUserRoleInfo(condition.getUserIds()));
	}

	@Inner
	@DeleteMapping(path = "/user/{userId}")
	public R findUserRoleInfo(@PathVariable(value = "userId") Integer userId) {
		return R.ok(sysInnerService.deleteUserByUserId(userId));
	}

	@Inner
	@PostMapping(path = "/role/add")
	//@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public R saveRole(@Valid @RequestBody SysRole sysRole) {
		return R.ok(sysInnerService.addRole(sysRole));
	}

	@Inner
	@PostMapping(path = "/role/update")
	//@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public R updateRole(@RequestBody SysRole sysRole) {
		return R.ok(sysInnerService.updateRole(sysRole));
	}
}
