package com.pig4cloud.pig.admin.sso.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.api.dto.MenuTree;
import com.pig4cloud.pig.admin.api.dto.TreeNode;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.LocalTokenHolder;
import com.pig4cloud.pig.admin.sso.common.ssoutil.SnowFlakeUtil;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.service.IRemoteService;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
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
public class SysMenu2SSOServiceImpl {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private IRemoteService remoteService;

	@Autowired
	private TokenStore tokenStore;

	private SnowFlakeUtil idWorker = new SnowFlakeUtil();


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

	private String findUserName(String token) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(oAuth2AccessToken);
		// 清空用户信息
		UserDetails userDetails = (UserDetails) cacheManager.getCache(CacheConstants.USER_DETAILS).get(auth2Authentication.getName());
		return userDetails.getUsername();
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

}
