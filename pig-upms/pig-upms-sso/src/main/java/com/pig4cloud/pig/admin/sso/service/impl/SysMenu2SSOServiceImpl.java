package com.pig4cloud.pig.admin.sso.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.api.dto.TreeNode;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.LocalTokenHolder;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName SysMenu2SSOServiceImpl
 * @Author Duys
 * @Description
 * @Date 2022/7/21 15:07
 **/
@Component
public class SysMenu2SSOServiceImpl extends BaseSysServiceImpl {



	public List<MenuTree> findMenuByPrentId() {
		// 获取所有的菜单
		// 1.拿到用户的token
		// 2.拿用户信息，再去拿serverToken
		// 3.请求所有的权限信息，解析menu
		// 4.封装返回
		String token = LocalTokenHolder.getToken();
		if (StringUtils.isEmpty(token)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		String userName = findUserName(token);
		String key = "@@" + userName.split("@@")[1];
		String serverToken = getServerToken(token + key);
		if (StringUtils.isEmpty(serverToken)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		Map<String, String> localLoginInfo = toLocalLogin(serverToken + key);
		Map ossClientInfoMap = getSSOClientInfo();
		List<SSOPrivilege> ssoPrivilege = remoteService.getSSOMenus(serverToken, localLoginInfo, ossClientInfoMap);
		List<MenuTree> list = new ArrayList<>();
		processMenu(ssoPrivilege, list);
		return list;
	}


	/**
	 * 也是按层遍历
	 *
	 * @param ssoPrivileges
	 * @param list
	 */
	public void processMenu(List<SSOPrivilege> ssoPrivileges, List<MenuTree> list) {
		if (CollectionUtils.isEmpty(ssoPrivileges)) {
			return;
		}
		for (SSOPrivilege privilege : ssoPrivileges) {
			MenuTree menuTree = new MenuTree();
			menuTree.setIcon(privilege.getExtPropertyInfo() != null ? privilege.getExtPropertyInfo().getPrivilege_Property_ICON() : "");
			menuTree.setLabel(privilege.getPrivilegeName());
			menuTree.setName(privilege.getPrivilegeName());
			menuTree.setType("0");
			menuTree.setId(idWorker.getIntId());
			menuTree.setPermission(privilege.getPrivilegeCode());
			menuTree.setPath(privilege.getExtPropertyInfo() != null ? privilege.getExtPropertyInfo().getPrivilege_Property_URL() : "");
			menuTree.setSort(privilege.getSequence());
			menuTree.setChildren(processMenuTreeChild(privilege, menuTree.getId()));
			if (!CollectionUtils.isEmpty(privilege.getSsoPrivileges())) {
				menuTree.setHasChildren(true);
			}
			list.add(menuTree);
		}
	}

	private List<TreeNode> processMenuTreeChild(SSOPrivilege privilege, Integer parentId) {
		List<TreeNode> ans = new ArrayList<>();
		if (Objects.isNull(privilege) || CollectionUtils.isEmpty(privilege.getSsoPrivileges())) {
			return ans;
		}
		for (SSOPrivilege child : privilege.getSsoPrivileges()) {
			MenuTree menuTree = new MenuTree();
			menuTree.setIcon(child.getExtPropertyInfo() != null ? child.getExtPropertyInfo().getPrivilege_Property_ICON() : "");
			menuTree.setLabel(child.getPrivilegeName());
			menuTree.setId(idWorker.getIntId());
			menuTree.setParentId(parentId);
			menuTree.setName(child.getPrivilegeName());
			menuTree.setType("0");
			menuTree.setPermission(child.getPrivilegeCode());
			menuTree.setPath(child.getExtPropertyInfo() != null ? child.getExtPropertyInfo().getPrivilege_Property_URL() : "");
			menuTree.setSort(child.getSequence());
			menuTree.setChildren(processMenuTreeChild(child, menuTree.getId()));
			if (!CollectionUtils.isEmpty(child.getSsoPrivileges())) {
				menuTree.setHasChildren(true);
			}
			ans.add(menuTree);
		}
		return ans;
	}




}
