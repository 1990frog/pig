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

package com.pig4cloud.pig.admin.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.api.dto.TreeNode;
import com.pig4cloud.pig.admin.api.entity.SysMenu;
import com.pig4cloud.pig.admin.api.util.TreeUtils;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.common.ssoutil.LocalTokenHolder;
import com.pig4cloud.pig.admin.common.ssoutil.SnowFlakeUtil;
import com.pig4cloud.pig.admin.model.SSOPrivilege;
import com.pig4cloud.pig.admin.service.IRemoteService;
import com.pig4cloud.pig.admin.service.SysMenuService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author lengleng
 * @since 2017-10-29
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private IRemoteService remoteService;

	private SnowFlakeUtil idWorker = new SnowFlakeUtil();

	@Override
	//@Cacheable(value = CacheConstants.MENU_DETAILS, key = "#roleId  + '_menu'", unless = "#result == null")
	public List<MenuTree> findMenuByRoleId(Integer roleId) {
		// 获取所有的菜单
		// 1.拿到用户的token，换serverToken
		// 2.请求所有的权限信息，解析menu
		// 3.封装返回
		String token = LocalTokenHolder.getToken();
		if (StringUtils.isEmpty(token)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		String serverToken = getServerToken(token);
		if (StringUtils.isEmpty(serverToken)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		Map<String, String> localLoginInfo = toLocalLogin(serverToken);
		Map ossClientInfoMap = getSSOClientInfo();
		List<SSOPrivilege> ssoPrivilege = remoteService.getSSOMenus(serverToken, localLoginInfo, ossClientInfoMap);
		List<MenuTree> list = new ArrayList<>();
		processMenu(ssoPrivilege, list);
		//throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
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

	private String getServerToken(String localToken) {
		Cache serverTokenCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN);
		if (Objects.isNull(serverTokenCache) || Objects.isNull(serverTokenCache.get(localToken))
				|| Objects.isNull(serverTokenCache.get(localToken).get())) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		String serverToken = (String) serverTokenCache.get(localToken).get();
		return serverToken;
	}

	// 本地登录使用的信息
	private Map<String, String> toLocalLogin(String serverToken) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		return (Map<String, String>) cache.get(serverToken).get();
	}

	// 拿ssoClientInfo
	private Map getSSOClientInfo() {
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		return ossClientInfoMap;
	}

	/**
	 * 级联删除菜单
	 *
	 * @param id 菜单ID
	 * @return true成功, false失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	//@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public Boolean removeMenuById(Integer id) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	//@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public Boolean updateMenuById(SysMenu sysMenu) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 构建树查询 1. 不是懒加载情况，查询全部 2. 是懒加载，根据parentId 查询 2.1 父节点为空，则查询ID -1
	 *
	 * @param lazy     是否是懒加载
	 * @param parentId 父节点ID
	 * @return
	 */
	@Override
	public List<MenuTree> treeMenu(boolean lazy, Integer parentId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 查询菜单
	 *
	 * @param all      全部菜单
	 * @param parentId 父节点ID
	 * @return
	 */
	@Override
	public List<MenuTree> filterMenu(Set<MenuVO> all, Integer parentId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 通过sysMenu创建树形节点
	 *
	 * @param menus
	 * @param root
	 * @return
	 */
	private List<MenuTree> buildTree(List<SysMenu> menus, int root) {
		List<MenuTree> trees = new ArrayList<>();
		MenuTree node;
		for (SysMenu menu : menus) {
			node = new MenuTree();
			node.setId(menu.getMenuId());
			node.setParentId(menu.getParentId());
			node.setName(menu.getName());
			node.setPath(menu.getPath());
			node.setPermission(menu.getPermission());
			node.setLabel(menu.getName());
			node.setIcon(menu.getIcon());
			node.setType(menu.getType());
			node.setSort(menu.getSort());
			node.setHasChildren(false);
			node.setKeepAlive(menu.getKeepAlive());
			trees.add(node);
		}
		return TreeUtils.build(trees, root);
	}

	/**
	 * 自定义实现查询角色已经绑定的菜单
	 */
	public List<MenuVO> findMenuInfoByRoleId(Integer roleId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

}
