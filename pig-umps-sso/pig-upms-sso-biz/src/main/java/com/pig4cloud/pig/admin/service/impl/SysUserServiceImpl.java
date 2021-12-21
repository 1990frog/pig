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

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.api.vo.UserVO;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.common.ssoutil.LocalTokenHolder;
import com.pig4cloud.pig.admin.common.ssoutil.SnowFlakeUtil;
import com.pig4cloud.pig.admin.model.SSOPrivilege;
import com.pig4cloud.pig.admin.model.SSORoleInfo;
import com.pig4cloud.pig.admin.service.IRemoteService;
import com.pig4cloud.pig.admin.service.SysUserService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.security.service.PigUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lengleng
 * @date 2019/2/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {


	private final CacheManager cacheManager;

	private final RedisTemplate<String, Object> redisTemplate;

	private final IRemoteService remoteService;

	private PasswordEncoder ENCODER = new BCryptPasswordEncoder();
	private SnowFlakeUtil idWorker = new SnowFlakeUtil();

	/**
	 * 保存用户信息
	 *
	 * @param userDto DTO 对象
	 * @return success/fail
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveUser(UserDTO userDto) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public SysUser getUserDetails(SysUser sysUser) {
		if (sysUser == null || StringUtils.isEmpty(sysUser.getUsername())
				|| StringUtils.isEmpty(sysUser.getSysClass())) {
			return null;
		}
		PigUser pigUser = getPigUser(sysUser.getUsername(), sysUser.getSysClass());
		SysUser res = new SysUser();
		res.setSysClass(pigUser.getSysClass());
		res.setUsername(pigUser.getUsername());
		res.setPassword(pigUser.getPassword());
		res.setLockFlag("0");
		res.setDelFlag("0");
		res.setDeptId(1);
		res.setUserId(1);
		return res;
	}

	@Override
	public List<SysUser> getAllUser() {
		Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS + "*");
		if (CollectionUtils.isEmpty(keys)) {
			return Collections.EMPTY_LIST;
		}
		ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
		if (Objects.isNull(stringObjectValueOperations)) {
			return Collections.EMPTY_LIST;
		}
		// pigUser
		List<Object> objects = stringObjectValueOperations.multiGet(keys);
		List<SysUser> all = new ArrayList<>();
		for (Object obj : objects) {
			PigUser pigUser = (PigUser) obj;
			SysUser res = new SysUser();
			res.setSysClass(pigUser.getSysClass());
			res.setUsername(pigUser.getUsername());
			res.setPassword(pigUser.getPassword());
			res.setLockFlag("0");
			res.setDelFlag("0");
			all.add(res);
		}
		return all;
	}

	/**
	 * 通过查用户的全部信息
	 *
	 * @param sysUser 用户
	 * @return
	 */
	@Override
	public UserInfo getUserInfo(SysUser sysUser) {
		// 内部调用的时候没有token
		if (sysUser == null || StringUtils.isEmpty(sysUser.getUsername())
				|| StringUtils.isEmpty(sysUser.getSysClass())) {
			String token = LocalTokenHolder.getToken();
			// 这儿就用token去获取
			return getUserInfoByToken(token);
		} else {
			// 就用名称去获取
			return fillUserInfo(sysUser.getUsername(), sysUser.getSysClass());
		}
	}

	private UserInfo fillUserInfo(String userName, String sysClass) {
		UserInfo userInfo = getUserInfo(userName, sysClass);
		if (!Objects.isNull(userInfo)) {
			return userInfo;
		}
		// 先拿serverToken
		String key = userName + "@@" + sysClass;
		String serverToken = getServerTokenByUserName(key);
		// 再拿用户登录信息
		Map serverTokenLoginInfo = toServerLogin(serverToken);
		if (Objects.isNull(serverTokenLoginInfo)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		// 再拿本地登录使用的信息
		Map<String, String> localLoginInfo = toLocalLogin(serverToken);
		if (Objects.isNull(localLoginInfo)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		//去远端拿信息了
		Cache ossClientInfo = cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO);
		Map ossClientInfoMap = (Map) ossClientInfo.get(CacheConstants.SSO_CLIENT_INFO).get();
		List<SSORoleInfo> ssoRoleInfo = remoteService.getSSORoleInfo(serverToken, localLoginInfo, ossClientInfoMap);
		List<SSOPrivilege> ssoPrivilege = remoteService.getSSOPrivilege(serverToken, localLoginInfo, ossClientInfoMap);
		fillPigUser(localLoginInfo, ssoRoleInfo, ssoPrivilege);
		return fillUserInfo(localLoginInfo, ssoRoleInfo, ssoPrivilege);
	}

	private UserInfo fillUserInfo(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		// 再拿一次
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(serverInfoMap.get("username")))
				&& !Objects.isNull(userInfoCache.get(serverInfoMap.get("username")).get())) {
			return (UserInfo) userInfoCache.get(serverInfoMap.get("username")).get();
		}
		String userCode = username.split("@@")[0];
		String sysClass = username.split("@@")[1];
		SysUser sysUser = new SysUser();
		sysUser.setUserId(idWorker.getIntId());
		sysUser.setDeptId(idWorker.getIntId());
		sysUser.setDelFlag("0");
		sysUser.setLockFlag("0");
		sysUser.setUsername(userCode);
		sysUser.setSysClass(sysClass);
		sysUser.setPassword(ENCODER.encode(serverInfoMap.get("password")));
		UserInfo userInfo = new UserInfo();
		userInfo.setSysUser(sysUser);
		// 设置角色列表 （ID）
		if (!org.springframework.util.CollectionUtils.isEmpty(ssoUserInfos)) {
			userInfo.setSsoRoles(ArrayUtil.toArray(ssoUserInfos.stream().map(SSORoleInfo::getRoleCode).collect(Collectors.toList()), String.class));
		}
		// 设置权限列表（menu.permission）
		if (!org.springframework.util.CollectionUtils.isEmpty(privileges)) {
			Set<String> pris = new HashSet<>();
			privileges.stream().filter(pri -> pri != null && pri.getExtPropertyInfo() != null)
					.forEach(ssoPrivilege -> pris.add(ssoPrivilege.getExtPropertyInfo().getPrivilege_Property_URL()));
			userInfo.setPermissions(ArrayUtil.toArray(pris, String.class));
		}
		if (userInfoCache != null) {
			userInfoCache.put(serverInfoMap.get("username"), userInfo);
		}
		return userInfo;
	}

	private PigUser fillPigUser(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		if (StringUtils.isEmpty(username)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		String userCode = username.split("@@")[0];
		String sysClass = username.split("@@")[1];
		Set<String> dbAuthsSet = new HashSet<>();
		//获取用户系统标识
		dbAuthsSet.add(SecurityConstants.SYS_CLASS + sysClass);
		if (ArrayUtil.isNotEmpty(ssoUserInfos)) {
			// 获取角色
			ssoUserInfos.stream().forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role.getRoleCode()));
		}
		if (ArrayUtil.isNotEmpty(privileges)) {
			// 获取资源
			privileges.stream().filter(pri -> pri.getExtPropertyInfo() != null && pri.getExtPropertyInfo().getPrivilege_Property_URL() != null)
					.forEach(pri -> dbAuthsSet.add(pri.getExtPropertyInfo().getPrivilege_Property_URL()));
		}
		Collection<? extends GrantedAuthority> authorities = AuthorityUtils
				.createAuthorityList(dbAuthsSet.toArray(new String[0]));

		// userId 和 deptId 两边系统有所差异，这儿默认给一个
		PigUser userDetails = new PigUser(idWorker.getIntId(), idWorker.getIntId(), sysClass, userCode,
				ENCODER.encode(serverInfoMap.get("password")),
				true, true, true, true, authorities);
		return userDetails;
	}


	private Map<String, String> toLocalLogin(String serverToken) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		return (Map<String, String>) cache.get(serverToken).get();
	}

	private Map toServerLogin(String serverToken) {
		Cache cache = cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE);
		if (cache != null && cache.get(serverToken) != null) {
			return (Map) cache.get(serverToken).get();
		}
		return null;
	}

	private String getServerTokenByUserName(String key) {
		Cache ssoClientInfoCache = cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN);
		return (String) ssoClientInfoCache.get(key).get();
	}


	private PigUser getPigUser(String userName, String sysClass) {
		String key = userName + "@@" + sysClass;
		Cache userDetailsCache = cacheManager.getCache(CacheConstants.USER_DETAILS);
		if (!Objects.isNull(userDetailsCache) && !Objects.isNull(userDetailsCache.get(key))
				&& !Objects.isNull(userDetailsCache.get(key))) {
			return (PigUser) userDetailsCache.get(key).get();
		}
		return null;
	}

	public UserInfo getUserInfoByToken(String localToken) {
		// 拿localToken换serverToken
		String serverToken = getServerToken(localToken);
		Map<String, String> serverInfoMap = getLocalLoginUserInfo(serverToken);
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(serverInfoMap.get("username")))
				&& !Objects.isNull(userInfoCache.get(serverInfoMap.get("username")).get())) {
			return (UserInfo) userInfoCache.get(serverInfoMap.get("username")).get();
		} else {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
	}

	private UserInfo getUserInfo(String userName, String sysClass) {
		String key = userName + "@@" + sysClass;
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(key))
				&& !Objects.isNull(userInfoCache.get(key).get())) {
			return (UserInfo) userInfoCache.get(key).get();
		}
		return null;
	}

	private Map<String, String> getLocalLoginUserInfo(String serverToken) {
		Cache serverInfo = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		if (Objects.isNull(serverInfo) || Objects.isNull(serverInfo.get(serverToken))
				|| Objects.isNull(serverInfo.get(serverToken).get())) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		return (Map<String, String>) serverInfo.get(serverToken).get();
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

	/**
	 * 分页查询用户信息（含有角色信息）
	 *
	 * @param page    分页对象
	 * @param userDTO 参数列表
	 * @return
	 */
	@Override
	public IPage getUserWithRolePage(Page page, UserDTO userDTO) {
		// 获取redis的可以
		Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS + "*");
		if (CollectionUtils.isEmpty(keys)) {
			return page;
		}
		Long pageNum = page.getCurrent();
		Long pageSize = page.getSize();
		// 比如 pageNum = 1  pageSize = 20 就是第一页20条 偏移量就是 0到20条
		// 比如 pageNum = 2  pageSize = 20 就是第二页20条 偏移量就是 20到40条
		//
		Long total = Long.valueOf(keys.size());
		IPage iPage = new Page();
		iPage.setCurrent(pageNum);
		iPage.setTotal(total);
		List<String> collect = keys.stream().collect(Collectors.toList());
		List<String> currentKeys = null;
		if (total > pageSize * pageNum) {
			// 需要分页
			// 向上取整。看看总共多少页
			// 总共61 条，每一页20条 总共4页
			// 要第三页
			int pageCount = (int) (Math.ceil(1.0 * total / pageSize));
			// 总共多少页
			iPage.setPages(pageCount);
			// 当前偏移量 0 - 19 20 -39 40 - 59 60-60
			long currentIndexStart = ((pageNum - 1) * pageSize);
			long currentIndexEnd = pageNum * pageSize - 1;
			iPage.setSize(currentIndexEnd - currentIndexStart);
			if (currentIndexEnd >= total - 1) {
				currentIndexEnd = total - 1;
			}
			// 左闭右开
			currentKeys = collect.subList((int) currentIndexStart, (int) currentIndexEnd + 1);
		} else {
			// 全部返回
			currentKeys = collect;
			iPage.setPages(1);
			iPage.setSize(currentKeys.size());
		}
		ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
		if (Objects.isNull(stringObjectValueOperations)) {
			return iPage;
		}
		List<Object> objects = stringObjectValueOperations.multiGet(currentKeys);
		iPage.setRecords(objects);
		return iPage;
	}

	/**
	 * 通过ID查询用户信息
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	@Override
	public UserVO getUserVoById(Integer id) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 删除用户
	 *
	 * @param sysUser 用户
	 * @return Boolean
	 */
	@Override
	//@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#sysUser.username + '@@' + #sysUser.sysClass")
	public Boolean removeUserById(SysUser sysUser) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	//@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username + '@@' + #userDto.sysClass")
	public Boolean updateUserInfo(UserDTO userDto) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	//@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username + '@@' + #userDto.sysClass")
	public Boolean updateUser(UserDTO userDto) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 查询上级部门的用户信息
	 *
	 * @param username 用户名
	 * @return R
	 */
	@Override
	public List<SysUser> listAncestorUsersByUsername(String username) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 根据用户ID列表查询用户信息
	 */
	@Override
	public List<UserVO> listUsersByUserIds(List<Integer> ids) {
		return null;
	}

	/**
	 * 查询上级部门的用户信息
	 *
	 * @param username 用户名
	 * @return R
	 */
	@Override
	public List<SysUser> listAncestorUsersByUsernameNew(String username, String sysClass) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean deleteUserByUserId(Integer userId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

}
