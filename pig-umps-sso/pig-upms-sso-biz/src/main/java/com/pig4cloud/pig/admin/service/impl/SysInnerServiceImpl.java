package com.pig4cloud.pig.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.condition.QueryRoleCondition;
import com.pig4cloud.pig.admin.api.dto.MenuTreeDTO;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserRoleDTO;
import com.pig4cloud.pig.admin.api.entity.SysMenu;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.service.SysInnerService;
import com.pig4cloud.pig.admin.service.SysMenuService;
import com.pig4cloud.pig.admin.service.SysRoleMenuService;
import com.pig4cloud.pig.admin.service.SysRoleService;
import com.pig4cloud.pig.admin.service.SysUserRoleService;
import com.pig4cloud.pig.admin.service.SysUserService;
import com.pig4cloud.pig.common.core.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName SysInnerServiceImpl
 * @Author Duys
 * @Description
 * @Date 2021/7/15 10:04
 **/
@Service
@Slf4j
public class SysInnerServiceImpl implements SysInnerService {

	@Autowired
	private SysRoleService sysRoleService;

	@Autowired
	private SysUserRoleService sysUserRoleService;

	@Autowired
	private SysMenuService sysMenuService;

	@Autowired
	private SysRoleMenuService sysRoleMenuService;

	@Autowired
	private SysUserService sysUserService;

	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

	@Override
	public Page<SysRole> pageByParam(QueryRoleCondition condition) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public List<UserRoleDTO> getUserRoleByUserId(QueryRoleCondition condition) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public List<MenuTreeDTO> getRoleMenus(QueryRoleCondition condition) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean edit(QueryRoleCondition condition) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}


	@Override
	@Transactional
	public Integer addUser(UserDTO userDTO) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	@Transactional
	public Boolean updateUser(UserDTO userDTO) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}


	@Override
	public List<UserRoleDTO> findUserRoleInfo(List<Integer> userIds) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean deleteUserByUserId(Integer userId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public SysRole addRole(SysRole sysRole) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean updateRole(SysRole sysRole) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean deleteRole(SysRole sysRole) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	private void processChild(Set<Integer> allMenuId, Map<Integer, List<SysMenu>> collect, SysMenu sysMenu) {
		// 这里就是子
		List<SysMenu> sysMenus = collect.get(sysMenu.getMenuId());
		if (CollectionUtils.isEmpty(sysMenus)) {
			return;
		}
		// 把当前的加入到结果中
		List<Integer> menuIds = sysMenus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(menuIds)) {
			allMenuId.addAll(menuIds);
		}
		for (SysMenu child : sysMenus) {
			if (Objects.isNull(child)) {
				continue;
			}
			// 继续吧-
			// 也需要把自己加进来
			allMenuId.add(child.getMenuId());
			processChild(allMenuId, collect, child);
		}
	}

	public void processTree(List<SysMenu> list, List<MenuTreeDTO> result) {
		// 按照父节点进行分组
		log.info("内部接口， 获取当前角色所有的菜单3，参数，{}", list == null ? 0 : list.size());
		Map<Integer, List<SysMenu>> collect = list.stream().collect(Collectors.groupingBy(SysMenu::getParentId));
		if (CollectionUtils.isEmpty(collect)) {
			return;
		}
		// 获取全部的父节点
		List<SysMenu> sysMenus = collect.get(CommonConstants.MENU_TREE_ROOT_ID);
		if (CollectionUtils.isEmpty(sysMenus)) {
			return;
		}
		// 保存的就是一条链路-头节点的
		sysMenus.stream().forEach(m -> {
			result.add(new MenuTreeDTO(m));
		});
		for (MenuTreeDTO menuTreeDTO : result) {
			// 这里的
			findChild(menuTreeDTO, collect);
		}
	}

	private void findChild(MenuTreeDTO prent, Map<Integer, List<SysMenu>> collect) {
		if (CollectionUtils.isEmpty(collect)) {
			return;
		}
		if (Objects.isNull(prent)) {
			return;
		}
		// 这里就是子
		List<SysMenu> sysMenus = collect.get(prent.getMenuId());
		if (CollectionUtils.isEmpty(sysMenus)) {
			return;
		}
		List<MenuTreeDTO> childs = new ArrayList<>();
		for (SysMenu sysMenu : sysMenus) {
			MenuTreeDTO menuTreeDTO = new MenuTreeDTO(sysMenu);
			childs.add(menuTreeDTO);
			// 以当前为父亲-继续吧找你的孩子
			findChild(menuTreeDTO, collect);
		}
		prent.setChild(childs);
	}

	/**
	 * 处理返回，递归去玩儿
	 *
	 * @param result
	 * @param map
	 * @param roleId
	 */
	private void processResult(List<MenuTreeDTO> result, Map<Integer, MenuVO> map, Integer roleId) {
		log.info("内部接口， 获取当前角色所有的菜单4，参数，{}", map == null ? 0 : map.size());
		for (MenuTreeDTO menuTreeDTO : result) {
			MenuVO menuVO = map.get(menuTreeDTO.getMenuId());
			if (!Objects.isNull(menuVO)) {
				menuTreeDTO.setHasPermission(true);
				menuTreeDTO.setRoleId(roleId);
			}
			List<MenuTreeDTO> child = menuTreeDTO.getChild();
			if (CollectionUtils.isEmpty(child)) {
				continue;
			}
			// 递归去吧，所有的孩子都设置一次
			processResult(child, map, roleId);
		}
	}

}
