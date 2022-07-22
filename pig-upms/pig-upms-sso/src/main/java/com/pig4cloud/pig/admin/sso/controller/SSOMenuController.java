package com.pig4cloud.pig.admin.sso.controller;

import com.pig4cloud.pig.admin.controller.MenuController;
import com.pig4cloud.pig.admin.service.SysMenuService;
import com.pig4cloud.pig.admin.sso.service.impl.SysMenu2SSOServiceImpl;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName SSOMenuController
 * @Author Duys
 * @Description
 * @Date 2022/7/21 16:00
 **/
@Component
public class SSOMenuController extends MenuController {

	@Autowired
	private SysMenu2SSOServiceImpl sysMenu2SSOService;

	@Autowired
	private CacheManager cacheManager;

	public SSOMenuController(SysMenuService sysMenuService) {
		super(sysMenuService);
	}

	public R getUserMenu(Integer parentId) {
		// 获取当前的ssoClintInfo
		Map ssoClientInfo = getSSOClientInfo();
		boolean enable = (Boolean) ssoClientInfo.get("enable");
		if (!enable) {
			return R.ok(super.getUserMenu(parentId).getData());
		} else {
			// 获取符合条件的菜单
			return R.ok(sysMenu2SSOService.findMenuByPrentId());
		}
	}

	// 拿ssoClientInfo
	private Map getSSOClientInfo() {
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		return ossClientInfoMap;
	}
}
