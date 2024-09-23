/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.dto.RoleMenuOperate;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.api.vo.RoleVo;
import com.pig4cloud.pig.admin.service.SysRoleMenuService;
import com.pig4cloud.pig.admin.service.SysRoleService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.Inner;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.LocalTokenHolder;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lengleng
 * @date 2019/2/1
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
@Api(value = "role", tags = "角色管理模块")
public class RoleController {

	private final SysRoleService sysRoleService;

	private final SysRoleMenuService sysRoleMenuService;

	private final TokenStore tokenStore;

	/**
	 * 通过ID查询角色信息
	 *
	 * @param id ID
	 * @return 角色信息
	 */
	@GetMapping("/{id}")
	public R getById(@PathVariable Integer id) {
		return R.ok(sysRoleService.getById(id));
	}

	/**
	 * 添加角色
	 *
	 * @param sysRole 角色信息
	 * @return success、false
	 */
	@SysLog("添加角色")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_role_add')")
	public R save(@Valid @RequestBody SysRole sysRole) {
		return R.ok(sysRoleService.save(sysRole));
	}

	/**
	 * 修改角色
	 *
	 * @param sysRole 角色信息
	 * @return success/false
	 */
	@SysLog("修改角色")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public R update(@Valid @RequestBody SysRole sysRole) {
		return R.ok(sysRoleService.updateById(sysRole));
	}

	/**
	 * 删除角色
	 *
	 * @param id
	 * @return
	 */
	@SysLog("删除角色")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public R removeById(@PathVariable Integer id) {
		return R.ok(sysRoleService.removeRoleById(id));
	}

	/**
	 * 获取角色列表
	 *
	 * @return 角色列表
	 */
	@GetMapping("/list")
	public R listRoles() {
		return R.ok(sysRoleService.list(Wrappers.emptyWrapper()));
	}

	/**
	 * 分页查询角色信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public R getRolePage(Page page, String sysClass) {
		SysRole role = new SysRole();
		List<String> sysClassList = new ArrayList<>();
		if (sysClass == null || sysClass.isEmpty()) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
			authorities.stream().filter(granted -> StrUtil.startWith(granted.getAuthority(), SecurityConstants.SYS_CLASS))
					.forEach(granted -> {
						String temp = StrUtil.removePrefix(granted.getAuthority(), SecurityConstants.SYS_CLASS);
						sysClassList.add(temp);
					});
			sysClass = sysClassList.get(0);
		}
		if (!"SUPER".equals(sysClass)) {
			role.setSysClass(sysClass);
		}
		return R.ok(sysRoleService.page(page, Wrappers.lambdaQuery(role)));
	}

	/**
	 * 更新角色菜单
	 *
	 * @param roleVo 角色对象
	 * @return success、false
	 */
	@SysLog("更新角色菜单")
	@PutMapping("/menu")
	@PreAuthorize("@pms.hasPermission('sys_role_perm')")
	public R saveRoleMenus(@RequestBody RoleVo roleVo) {
		SysRole sysRole = sysRoleService.getById(roleVo.getRoleId());
		return R.ok(sysRoleMenuService.saveRoleMenus(sysRole.getRoleCode(), roleVo.getRoleId(), roleVo.getMenuIds()));
	}

	/**
	 * 根据code获取角色
	 *
	 * @return 角色
	 */
	@GetMapping("/one")
	public R roleByCode(@RequestParam String roleCode) {
		SysRole condition = new SysRole();
		condition.setRoleCode(roleCode);
		return R.ok(sysRoleService.getOne(new QueryWrapper<>(condition)));
	}

	/**
	 * 根据code删除角色
	 *
	 * @return 角色
	 */
	@SysLog("删除角色")
	@PostMapping("/del")
	@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public R deleteByCode(@Valid @RequestBody SysRole sysRole) {
		return R.ok(sysRoleService.updateSelective(sysRole));
	}

	/**
	 * 角色权限操作
	 *
	 * @return 角色
	 */
	@SysLog("角色权限新增和删除")
	@PostMapping("/operate")
	@PreAuthorize("@pms.hasPermission('sys_role_del') and @pms.hasPermission('sys_role_add')")
	public R operate(@Valid @RequestBody RoleMenuOperate roleMenuOperate) {
		return R.ok(sysRoleService.operate(roleMenuOperate));
	}

	/**
	 * 获取角色列表
	 *
	 * @return 角色列表
	 */
	@Inner
	@GetMapping("/list/{sysClass}")
	public List<SysRole> getRoleList(@PathVariable("sysClass") String sysClass) {
		return sysRoleService.getRoleList(sysClass);
	}

	@GetMapping("/info/list/current")
	public List<SysRole> getRoleByCurrent() {
		PigUser user = SecurityUtils.getUser();
		return sysRoleService.findRolesByUserId(user.getId());
	}

	@GetMapping("/info/list/all")
	public List<SysRole> getRoleByAll() {
		return sysRoleService.list();
	}
}
