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

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.api.entity.SysMenu;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.api.entity.SysRoleMenu;
import com.pig4cloud.pig.admin.api.util.TreeUtils;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.admin.mapper.SysMenuMapper;
import com.pig4cloud.pig.admin.mapper.SysRoleMenuMapper;
import com.pig4cloud.pig.admin.service.SysMenuService;
import com.pig4cloud.pig.admin.service.SysRoleMenuService;
import com.pig4cloud.pig.admin.service.SysRoleService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.CommonConstants;
import com.pig4cloud.pig.common.core.constant.enums.MenuTypeEnum;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pig4cloud.pig.common.core.constant.CommonConstants.SUPER_ADMIN;

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
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

	private final SysRoleMenuMapper sysRoleMenuMapper;

	private final SysRoleMenuService sysRoleMenuService;

	@Override
	@Cacheable(value = CacheConstants.MENU_DETAILS, key = "#roleId  + '_menu'", unless = "#result == null")
	public List<MenuVO> findMenuByRoleId(Integer roleId) {
		List<SysRoleMenu> sysRoleMenus = sysRoleMenuService.lambdaQuery().eq(SysRoleMenu::getRoleId, roleId).list();
		if (CollectionUtils.isEmpty(sysRoleMenus)) {
			return Collections.emptyList();
		}
		List<SysMenu> sysMenus = this.lambdaQuery()
				.in(SysMenu::getMenuId, sysRoleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()))
				.eq(SysMenu::getDelFlag, "0")
				.list();
		if (CollectionUtils.isEmpty(sysMenus)) {
			return Collections.emptyList();
		}
		List<MenuVO> ans = new ArrayList<>();
		for (SysMenu sysMenu : sysMenus) {
			MenuVO menuVO = new MenuVO();
			BeanUtils.copyProperties(sysMenu, menuVO);
			ans.add(menuVO);
		}
		//return baseMapper.listMenusByRoleId(roleId);
		return ans;
	}

	/**
	 * 
	 * @param system 系统
	 * @return
	 */
	@Override
	public List<MenuVO> findMenuBySystem(String system) {
		return baseMapper.findMenuBySystem(system);
	}

	/**
	 * 级联删除菜单
	 *
	 * @param id 菜单ID
	 * @return true成功, false失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public Boolean removeMenuById(Integer id) {
		// 查询父节点为当前节点的节点
		List<SysMenu> menuList = this.list(Wrappers.<SysMenu>query().lambda().eq(SysMenu::getParentId, id));

		Assert.isTrue(CollUtil.isEmpty(menuList), "菜单含有下级不能删除");

		sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>query().lambda().eq(SysRoleMenu::getMenuId, id));
		// 删除当前菜单及其子菜单
		return this.removeById(id);
	}

	@Override
	@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public Boolean updateMenuById(SysMenu sysMenu) {
		return this.updateById(sysMenu);
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
		String sysClass = Optional.ofNullable(SecurityUtils.getUser()).map(PigUser::getSysClass).orElse(null);
		final LambdaQueryWrapper<SysMenu> sysMenuLambdaQueryWrapper = Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort);
		if (!SUPER_ADMIN.equalsIgnoreCase(sysClass)) {
			sysMenuLambdaQueryWrapper.eq(SysMenu::getSysClass, sysClass);
		}
		if (!lazy) {
			return buildTree(baseMapper.selectList(sysMenuLambdaQueryWrapper),
					CommonConstants.MENU_TREE_ROOT_ID);
		}

		Integer parent = parentId == null ? CommonConstants.MENU_TREE_ROOT_ID : parentId;

		return buildTree(
				baseMapper.selectList(
						sysMenuLambdaQueryWrapper.eq(SysMenu::getParentId, parent)),
				parent);
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
		List<MenuTree> menuTreeList = all.stream().filter(vo -> MenuTypeEnum.LEFT_MENU.getType().equals(vo.getType()))
				.map(MenuTree::new).sorted(Comparator.comparingInt(MenuTree::getSort)).collect(Collectors.toList());
		Integer parent = parentId == null ? CommonConstants.MENU_TREE_ROOT_ID : parentId;
		return TreeUtils.build(menuTreeList, parent);
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
			node.setSysClass(menu.getSysClass());
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
		return baseMapper.listMenusByRoleId(roleId);
	}

}
