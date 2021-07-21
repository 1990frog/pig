package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.condition.QueryRoleCondition;
import com.pig4cloud.pig.admin.api.dto.MenuTreeDTO;
import com.pig4cloud.pig.admin.api.dto.RoleInfoDTO;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserRoleDTO;
import com.pig4cloud.pig.admin.api.entity.SysMenu;
import com.pig4cloud.pig.admin.api.entity.SysRole;
import com.pig4cloud.pig.admin.api.entity.SysRoleMenu;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.api.entity.SysUserRole;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.admin.service.SysInnerService;
import com.pig4cloud.pig.admin.service.SysMenuService;
import com.pig4cloud.pig.admin.service.SysRoleMenuService;
import com.pig4cloud.pig.admin.service.SysRoleService;
import com.pig4cloud.pig.admin.service.SysUserRoleService;
import com.pig4cloud.pig.admin.service.SysUserService;
import com.pig4cloud.pig.common.core.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
		log.info("内部接口， 批量获取角色信息，参数，{}", condition);
		Page<SysRole> page = new Page<>();
		page.setCurrent(condition.getPage() == null || condition.getPage() <= 0 ? 1 : condition.getPage());
		page.setSize(condition.getSize() == null || condition.getSize() <= 0 ? 10 : condition.getSize());
		return sysRoleService.lambdaQuery()
				.eq(StringUtils.isNotBlank(condition.getSysClass()), SysRole::getSysClass, condition.getSysClass())
				.eq(SysRole::getDelFlag, "0")
				.like(StringUtils.isNotBlank(condition.getRoleName()), SysRole::getRoleName, condition.getRoleName())
				.page(page);
	}

	@Override
	public List<UserRoleDTO> getUserRoleByUserId(QueryRoleCondition condition) {
		log.info("内部接口， 批量获取用户信息，参数，{}", condition);
		if (Objects.isNull(condition) || CollectionUtils.isEmpty(condition.getUserIds())) {
			return Collections.emptyList();
		}
		// 根据用户id获取角色信息
		List<SysUserRole> sysUserRoles = sysUserRoleService.lambdaQuery()
				.in(SysUserRole::getUserId, condition.getUserIds())
				.list();
		if (CollectionUtils.isEmpty(sysUserRoles)) {
			return Collections.emptyList();
		}
		List<Integer> roleIds = sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(roleIds)) {
			return Collections.emptyList();
		}
		// 查询角色信息
		List<SysRole> sysRoles = sysRoleService.lambdaQuery()
				.eq(StringUtils.isNotBlank(condition.getSysClass()), SysRole::getSysClass, condition.getSysClass())
				.in(SysRole::getRoleId, roleIds)
				// 只查询正常的
				.eq(SysRole::getDelFlag, "0").list();
		// 可能会存在一个用户绑定了多个角色
		if (CollectionUtils.isEmpty(sysRoles)) {
			return Collections.emptyList();
		}
		Map<Integer, List<SysRole>> sysRoleGroupByRoleId = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getRoleId));
		// 组装返回信息了
		List<UserRoleDTO> userRoleDTOS = new ArrayList<>();
		for (SysUserRole sysUserRole : sysUserRoles) {
			if (Objects.isNull(sysUserRole)) {
				continue;
			}
			UserRoleDTO userRoleDTO = new UserRoleDTO();
			Integer userId = sysUserRole.getUserId();
			Integer roleId = sysUserRole.getRoleId();
			userRoleDTO.setUserId(userId);
			List<SysRole> sysRoles1 = sysRoleGroupByRoleId.get(roleId);
			if (!CollectionUtils.isEmpty(sysRoles1)) {
				List<RoleInfoDTO> roleInfoDTOS = new ArrayList<>();
				sysRoles1.stream().filter(role -> !Objects.isNull(role)).forEach(role -> {
					roleInfoDTOS.add(new RoleInfoDTO(role.getRoleId(), role.getRoleName()));
				});
				if (!CollectionUtils.isEmpty(roleInfoDTOS)) {
					userRoleDTO.setRoleInfos(roleInfoDTOS);
				}
			}
			userRoleDTOS.add(userRoleDTO);
		}
		return userRoleDTOS;
	}

	@Override
	public List<MenuTreeDTO> getRoleMenus(QueryRoleCondition condition) {
		log.info("内部接口， 获取当前角色所有的菜单，参数，{}", condition);
		// 获取所有的菜单
		List<SysMenu> list = sysMenuService.lambdaQuery().eq(SysMenu::getDelFlag, "0").list();
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		List<MenuVO> menuByRoleId = sysMenuService.findMenuByRoleId(condition.getRoleId());
		Map<Integer, MenuVO> map = null;
		if (CollectionUtils.isEmpty(menuByRoleId)) {
			map = Collections.EMPTY_MAP;
		} else {
			map = menuByRoleId.stream().collect(Collectors.toMap(MenuVO::getMenuId, Function.identity()));
		}
		List<MenuTreeDTO> result = new ArrayList<>();
		processTree(list, result);

		// 设置是否绑定
		processResult(result, map, condition.getRoleId());
		return result;
	}

	@Override
	public Boolean edit(QueryRoleCondition condition) {
		log.info("内部接口， 编辑菜单信息的角色，参数，{}", condition);
		if (Objects.isNull(condition) || CollectionUtils.isEmpty(condition.getMenuIds())) {
			return false;
		}
		List<SysMenu> all = sysMenuService.lambdaQuery().eq(SysMenu::getDelFlag, "0").list();
		if (CollectionUtils.isEmpty(all)) {
			return false;
		}
		Map<Integer, List<SysMenu>> collect = all.stream().collect(Collectors.groupingBy(SysMenu::getParentId));

		// 根据菜单id获取所有菜单信息
		List<SysMenu> list = sysMenuService.lambdaQuery().in(SysMenu::getMenuId, condition.getMenuIds()).list();
		if (CollectionUtils.isEmpty(list)) {
			return false;
		}
		//
		Set<Integer> allMenuId = new HashSet<>();
		for (SysMenu sysMenu : list) {
			if (Objects.isNull(sysMenu)) {
				continue;
			}
			// 先把自己加进去
			allMenuId.add(sysMenu.getMenuId());
			processChild(allMenuId, collect, sysMenu);
		}
		if (CollectionUtils.isEmpty(allMenuId)) {
			return false;
		}
		// 先干掉，再批量增加
		boolean remove = sysRoleMenuService.lambdaUpdate().eq(SysRoleMenu::getRoleId, condition.getRoleId()).remove();
		if (!remove) {
			return remove;
		}
		List<SysRoleMenu> entityList = new ArrayList<>();
		for (Integer menuId : allMenuId) {
			SysRoleMenu sysRoleMenu = new SysRoleMenu();
			sysRoleMenu.setRoleId(condition.getRoleId());
			sysRoleMenu.setMenuId(menuId);
			entityList.add(sysRoleMenu);
		}
		return sysRoleMenuService.saveBatch(entityList);
	}


	@Override
	@Transactional
	public Integer addUser(UserDTO userDTO) {
		log.info("内部接口， 新增用户，参数，{}", userDTO);
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDTO, sysUser);
		sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
		sysUser.setPassword(ENCODER.encode(userDTO.getPassword()));
		int insert = sysUserService.getBaseMapper().insert(sysUser);
		if (insert <= 0) {
			return insert;
		}
		if (!CollectionUtils.isEmpty(userDTO.getRole())) {
			List<SysUserRole> userRoleList = userDTO.getRole().stream().map(roleId -> {
				SysUserRole userRole = new SysUserRole();
				userRole.setUserId(sysUser.getUserId());
				userRole.setRoleId(roleId);
				return userRole;
			}).collect(Collectors.toList());
			sysUserRoleService.saveBatch(userRoleList);
		}
		return sysUser.getUserId();
	}

	@Override
	@Transactional
	public Boolean updateUser(UserDTO userDTO) {
		log.info("内部接口， 编辑用户，参数，{}", userDTO);
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDTO, sysUser);
		sysUser.setUpdateTime(LocalDateTime.now());

		if (StrUtil.isNotBlank(userDTO.getPassword())) {
			sysUser.setPassword(ENCODER.encode(userDTO.getPassword()));
		}
		sysUserService.updateById(sysUser);
		// 不需要更新角色信息
		if (CollectionUtils.isEmpty(userDTO.getRole())) {
			return Boolean.TRUE;
		}
		sysUserRoleService
				.remove(Wrappers.<SysUserRole>update().lambda().eq(SysUserRole::getUserId, userDTO.getUserId()));
		userDTO.getRole().forEach(roleId -> {
			SysUserRole userRole = new SysUserRole();
			userRole.setUserId(sysUser.getUserId());
			userRole.setRoleId(roleId);
			userRole.insert();
		});
		return Boolean.TRUE;
	}


	@Override
	public List<UserRoleDTO> findUserRoleInfo(List<Integer> userIds) {
		log.info("内部接口， 获取用户的角色信息，参数，{}", userIds);

		if (CollectionUtils.isEmpty(userIds)) {
			return Collections.emptyList();
		}
		List<SysUserRole> sysUserRoles = sysUserRoleService.lambdaQuery().in(SysUserRole::getUserId, userIds).list();
		if (CollectionUtils.isEmpty(sysUserRoles)) {
			return Collections.emptyList();
		}
		List<Integer> roleIds = sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(roleIds)) {
			return Collections.emptyList();
		}
		List<SysRole> sysRoles = sysRoleService.lambdaQuery().in(SysRole::getRoleId, roleIds).list();
		if (CollectionUtils.isEmpty(sysRoles)) {
			return Collections.emptyList();
		}
		// 一个用户可能会有多个角色
		Map<Integer, List<SysUserRole>> sysUserMap = sysUserRoles.stream().collect(Collectors.groupingBy(SysUserRole::getUserId));

		Map<Integer, SysRole> sysRoleMap = sysRoles.stream().collect(Collectors.toMap(SysRole::getRoleId, Function.identity()));

		List<UserRoleDTO> result = new ArrayList<>();

		for (Integer userId : sysUserMap.keySet()) {
			UserRoleDTO userRoleDTO = new UserRoleDTO();
			userRoleDTO.setUserId(userId);
			List<RoleInfoDTO> roleInfoDTOS = new ArrayList<>();
			List<SysUserRole> sysUserRole = sysUserMap.get(userId);
			if (CollectionUtils.isEmpty(sysUserRole)) {
				continue;
			}
			for (SysUserRole sysRole : sysUserRole) {
				if (Objects.isNull(sysRole)) {
					continue;
				}
				SysRole sysRoleEntity = sysRoleMap.get(sysRole.getRoleId());
				if (Objects.isNull(sysRoleEntity)) {
					continue;
				}
				RoleInfoDTO roleInfoDTO = new RoleInfoDTO();
				roleInfoDTO.setRoleId(sysRoleEntity.getRoleId());
				roleInfoDTO.setRoleName(sysRoleEntity.getRoleName());
				roleInfoDTOS.add(roleInfoDTO);
			}
			userRoleDTO.setRoleInfos(roleInfoDTOS);
			result.add(userRoleDTO);
		}
		return result;
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
