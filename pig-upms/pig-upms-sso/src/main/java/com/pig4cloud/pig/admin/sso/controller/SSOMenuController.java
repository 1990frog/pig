package com.pig4cloud.pig.admin.sso.controller;

import com.pig4cloud.pig.admin.sso.service.impl.SysMenu2SSOServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * @ClassName SSOMenuController
 * @Author Duys
 * @Description
 * @Date 2022/7/21 16:00
 **/
@Component
public class SSOMenuController {

	@Autowired
	private SysMenu2SSOServiceImpl sysMenu2SSOService;


	public R getUserMenu() {
		// 获取符合条件的菜单
		return R.ok(sysMenu2SSOService.findMenuByPrentId());
	}

}
