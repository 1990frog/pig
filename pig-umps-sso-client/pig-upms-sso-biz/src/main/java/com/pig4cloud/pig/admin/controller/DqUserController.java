package com.pig4cloud.pig.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.service.SysUserService;
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
import java.util.List;

/**
 * @author caijingquan@clinbrain.com
 * @date 2021/7/23
 * @desc 全部加inner，都走内部调用，UserController的方法加@Inner会影响现有功能
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dq/user")
@Api(value = "dq-user", tags = "用户管理模块")
public class DqUserController {

	private final SysUserService userService;

	/**
	 * dq：查询全部用户
	 *
	 * @param userDTO 查询参数列表
	 * @return 用户集合
	 */
	@Inner
	@GetMapping("/list")
	public List<SysUser> getUserList(UserDTO userDTO) {
		return userService.getAllUser();
	}

	/**
	 * 分页查询用户
	 *
	 * @param page    参数集
	 * @param userDTO 查询参数列表
	 * @return 用户集合
	 */
	@Inner
	@GetMapping("/page")
	public R getUserPage(Page page, UserDTO userDTO) {
		return R.ok(userService.getUserWithRolePage(page, userDTO));
	}

	/**
	 * 通过ID查询用户信息
	 *
	 * @param id ID
	 * @return 用户信息
	 */
	@Inner
	@GetMapping("/{id}")
	public R user(@PathVariable Integer id) {
		return R.ok(userService.getUserVoById(id));
	}

	/**
	 * 添加用户
	 *
	 * @param userDto 用户信息
	 * @return success/false
	 */
	@Inner
	@PostMapping
	public R user(@RequestBody UserDTO userDto) {
		return R.ok(userService.saveUser(userDto));
	}

	/**
	 * 更新用户信息
	 *
	 * @param userDto 用户信息
	 * @return R
	 */
	@Inner
	@PutMapping
	public R updateUser(@Valid @RequestBody UserDTO userDto) {
		return R.ok(userService.updateUser(userDto));
	}

	/**
	 * 删除用户
	 *
	 * @param id 用户id
	 * @return R
	 */
	@Inner
	@DeleteMapping("/{id}")
	public R userDel(@PathVariable Integer id) {
		return R.ok(userService.removeUserById(null));
	}
}
