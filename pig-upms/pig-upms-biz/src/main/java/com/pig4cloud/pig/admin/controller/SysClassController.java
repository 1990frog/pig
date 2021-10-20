package com.pig4cloud.pig.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.entity.SysSystem;
import com.pig4cloud.pig.admin.service.SysClassService;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 *
 * @description  系统类别控制器
 *
 * @author hexun
 * @date 17:49 2021/7/14
 *
 */
@RestController
@RequestMapping("/sys")
@Api(value = "sys", tags = "系统分类管理模块")
@RequiredArgsConstructor
public class SysClassController {


	private final SysClassService sysClassService;

	@GetMapping("/all")
	@PreAuthorize("@pms.hasPermission('sys_sys_search')")
	public R all() {
		return R.ok(sysClassService.list());
	}
	/**
	 * 分页获取系统信息
	 */
	@GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('sys_sys_search')")
	public R page(Page page, SysSystem sysSystem) {
		return R.ok(sysClassService.getPageSysClass(page, sysSystem));
	}

	@GetMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_sys_search')")
	public R sys(@PathVariable Integer id) {
		return R.ok(sysClassService.getSysClassById(id));
	}

	@SysLog("添加系统")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_sys_add')")
	public R user(@RequestBody SysSystem sysSystem) {
		return R.ok(sysClassService.addSysClass(sysSystem));
	}

	@SysLog("删除系统信息")
	@PreAuthorize("@pms.hasPermission('sys_sys_del')")
	@DeleteMapping("/{id}")
	public R del(@PathVariable Integer id) {
		return R.ok(sysClassService.deleteSysClass(id));
	}

	@SysLog("修改系统信息")
	@PreAuthorize("@pms.hasPermission('sys_sys_edit')")
	@PutMapping
	public R edit(@Valid @RequestBody SysSystem sysSystem) {
		return R.ok(sysClassService.editSysClass(sysSystem));
	}
}
