package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.api.entity.SysSystem;

public interface SysClassService {

	/**
	 * @description 根据ID获取指定系统
	 *
	 * @author hexun
	 * @param  sysId
	 * @return
	 */
	SysSystem getSysClassById(Integer sysId);

	/**
	 * @description 分页获取系统列表
	 *
	 * @author hexun
	 * @param page 分页对象
	 * @param sysSystem 条件筛选对象
	 * @return
	 */
	IPage getPageSysClass(Page page, SysSystem sysSystem);

	/**
	 * @description 根据ID删除系统信息
	 *
	 * @author hexun
	 * @param sysId 系统id
	 * @return
	 */
	Boolean deleteSysClass(Integer sysId);

	/**
	 * @description 修改系统信息
	 *
	 * @author hexun
	 * @param sysSystem 待更新系统
	 * @return
	 */
	Boolean editSysClass(SysSystem sysSystem);

	/**
	 * @description 添加系统
	 *
	 * @author hexun
	 * @param sysSystem 待添加系统
	 * @return
	 */
	Boolean addSysClass(SysSystem sysSystem);
}
