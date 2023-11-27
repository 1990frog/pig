package com.pig4cloud.pig.admin.sso.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.api.entity.UserExtendInfo;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SSOTypeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.common.security.util.LocalTokenHolder;
import com.pig4cloud.pig.admin.sso.common.ssoutil.SnowFlakeUtil;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.sso.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.model.SSORoleInfo;
import com.pig4cloud.pig.admin.sso.model.SoapEntity;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.security.service.PigUser;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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
 * @ClassName SysUser2SSOServiceImpl
 * @Author Duys
 * @Description
 * @Date 2022/7/21 17:15
 **/
@Component
public class SysUser2SSOServiceImpl extends BaseSysServiceImpl {


	private PasswordEncoder ENCODER = new BCryptPasswordEncoder();
	private SnowFlakeUtil idWorker = new SnowFlakeUtil();


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
	public UserInfo getUserInfo(SysUser sysUser) {
		// 内部调用的时候没有token
		String token = LocalTokenHolder.getToken();
		UserInfo userInfo = null;
		if ((sysUser == null || StringUtils.isEmpty(sysUser.getUsername())
				|| StringUtils.isEmpty(sysUser.getSysClass())) && !StrUtil.isEmpty(token)) {
			// 这儿就用token去获取
			userInfo = getUserInfoByToken(token);
		} else {
			// 就用名称去获取
			userInfo = fillUserInfo(sysUser.getUsername(), sysUser.getSysClass());
		}
		if (userInfo == null) {
			return null;
		}
		String username = userInfo.getSysUser().getUsername();
		userInfo.getSysUser().setUsername(username.split("@@")[0]);
		return userInfo;
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
		Map serverTokenLoginInfo = toServerLogin(serverToken + "@@" + sysClass);
		if (Objects.isNull(serverTokenLoginInfo)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		// 再拿本地登录使用的信息
		Map<String, String> localLoginInfo = toLocalLogin(serverToken + "@@" + sysClass);
		if (Objects.isNull(localLoginInfo)) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		//去远端拿信息了
		Map ossClientInfoMap = getSSOClientInfo();
		List<SSORoleInfo> ssoRoleInfo = remoteService.getSSORoleInfo(serverToken, localLoginInfo, ossClientInfoMap);
		List<SSOPrivilege> ssoPrivilege = remoteService.getSSOPrivilege(serverToken, localLoginInfo, ossClientInfoMap);
		//PigUser pigUser = fillPigUser(localLoginInfo, ssoRoleInfo, ssoPrivilege);
		UserInfo fillUserInfo = fillUserInfo(localLoginInfo, ssoRoleInfo, ssoPrivilege);
		return fillUserInfo;
	}

	private UserInfo fillUserInfo(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		String userCode = serverInfoMap.get("userCode");
		String sysClass = serverInfoMap.get("sysClass");
		if (StringUtils.isEmpty(username)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
		String key = userCode + "@@" + sysClass;
		// 再拿一次
		Cache userInfoCache = cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE);
		if (!Objects.isNull(userInfoCache) && !Objects.isNull(userInfoCache.get(key))) {
			return (UserInfo) userInfoCache.get(key).get();
		}
		SysUser sysUser = new SysUser();
		sysUser.setUserId(idWorker.getIntId());
		sysUser.setDeptId(idWorker.getIntId());
		sysUser.setDelFlag("0");
		sysUser.setLockFlag("0");
		sysUser.setUsername(username);
		sysUser.setSysClass(sysClass);
		sysUser.setPassword(ENCODER.encode(serverInfoMap.get("password")));
		UserInfo userInfo = new UserInfo();
		userInfo.setSysUser(sysUser);
		userInfo.setUserCode(userCode);
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
			userInfoCache.put(key, userInfo);
		}
		return userInfo;
	}

	private PigUser fillPigUser(Map<String, String> serverInfoMap, List<SSORoleInfo> ssoUserInfos, List<SSOPrivilege> privileges) {
		String username = serverInfoMap.get("username");
		String userCode = serverInfoMap.get("userCode");
		String sysClass = serverInfoMap.get("sysClass");
		if (StringUtils.isEmpty(username)) {
			throw new SSOBusinessException(ResponseCodeEnum.USER_INFO_NOT_EXIST);
		}
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

		PigUser userDetails = new PigUser(sysClass, userCode, username,
				ENCODER.encode(serverInfoMap.get("password")),
				true, true, true, true, authorities);
		return userDetails;
	}

	private Map<String, String> getLocalLoginUserInfo(String serverToken) {
		Cache serverInfo = cacheManager.getCache(CacheConstants.SSO_SERVER_INFO);
		if (Objects.isNull(serverInfo) || Objects.isNull(serverInfo.get(serverToken))
				|| Objects.isNull(serverInfo.get(serverToken).get())) {
			throw new SSOBusinessException(ResponseCodeEnum.LOGIN_EXPIRED);
		}
		return (Map<String, String>) serverInfo.get(serverToken).get();
	}

	/**
	 * 分页查询用户信息（含有角色信息）
	 *
	 * @return
	 */
	public IPage<UserExtendInfo> getUserWithRolePage(String userName, Long current, Long size) {
		Map ossClientInfoMap = getSSOClientInfo();
		Page<UserExtendInfo> result = new Page<>(current, size);
		try {
			Integer type = (Integer) ossClientInfoMap.get("type");
			SSOTypeEnum ssoType = SSOTypeEnum.parse(type);
			if (SSOTypeEnum.SOAP_1_2.equals(ssoType)) {
				Integer userCount = remoteService.findUserCount(userName, ossClientInfoMap);
				List<UserExtendInfo> userInfo = remoteService.findUserInfo(userName, current, size, ossClientInfoMap);
				result.setTotal(userCount);
				result.setRecords(userInfo);
				return result;
			}
			String token = LocalTokenHolder.getToken();
			String serverToken = getServerToken(token);
			// sso新版的走这里
			return remoteService.findUserInfo(current, size, serverToken, ossClientInfoMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public IPage getUserWithRolePageOld(Page page, UserDTO userDTO) {
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


}
