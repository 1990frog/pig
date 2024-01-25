package com.pig4cloud.pig.admin.sso.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.sso.service.impl.SysUser2SSOServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName SSOUserController
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:10
 **/
@Component
public class SSOUserController {

	@Autowired
	private SysUser2SSOServiceImpl sysUser2SSOService;


	public R userNew(String username, String sysClass) {
		SysUser condition = new SysUser();
		condition.setUsername(username);
		condition.setSysClass(sysClass);
		return R.ok(sysUser2SSOService.getUserDetails(condition));
	}

	public R info() {
		UserInfo userInfo = sysUser2SSOService.getUserInfo(null);
		if (userInfo == null || userInfo.getSysUser() == null) {
			return R.ok(userInfo);
		}
		SysUser sysUser = userInfo.getSysUser();
		String username = StrUtil.isEmpty(sysUser.getUsername()) ? "" : sysUser.getUsername();
		sysUser.setUsername(username.split("@@")[0]);
		userInfo.setSysUser(sysUser);
		return R.ok(userInfo);
	}

	public R infoNew(String username, String sysClass) {
		SysUser sysUser = new SysUser();
		sysUser.setUsername(username); // 用的是userCode
		sysUser.setSysClass(sysClass);
		return R.ok(sysUser2SSOService.getUserInfo(sysUser));
	}

	public R getUserExtendPage(String userName, Long current, Long size) {
		return R.ok(sysUser2SSOService.getUserWithRolePage(userName, current, size));
	}

	public R getUserPageList(Page page, UserDTO userDTO) {
		return R.ok(sysUser2SSOService.getUserWithRolePageOld(page, userDTO));
	}
}
