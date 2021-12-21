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

import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.api.entity.SysMenu;
import com.pig4cloud.pig.admin.api.util.TreeUtils;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
public class SysMenuServiceImpl  implements SysMenuService {


	@Override
	//@Cacheable(value = CacheConstants.MENU_DETAILS, key = "#roleId  + '_menu'", unless = "#result == null")
	public List<MenuVO> findMenuByRoleId(Integer roleId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
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
