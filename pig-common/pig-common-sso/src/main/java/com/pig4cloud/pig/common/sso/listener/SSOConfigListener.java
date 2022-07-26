package com.pig4cloud.pig.common.sso.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @ClassName SSOConfigListener
 * @Author Duys
 * @Description
 * @Date 2022/7/26 13:40
 **/
public class SSOConfigListener extends AbstractConfigChangeListener {
	private final String prefix = "sso";
	private final String ACCESS = "access:";
	private final String AUTH_TO_ACCESS = "auth_to_access:";
	private final String AUTH = "auth:";
	private final String REFRESH_AUTH = "refresh_auth:";
	private final String ACCESS_TO_REFRESH = "access_to_refresh:";
	private final String REFRESH = "refresh:";
	private final String REFRESH_TO_ACCESS = "refresh_to_access:";
	private final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
	private final String UNAME_TO_ACCESS = "uname_to_access:";

	private CacheManager cacheManager;
	private RedisTemplate redisTemplate;

	public SSOConfigListener(CacheManager cacheManager, RedisTemplate redisTemplate) {
		this.cacheManager = cacheManager;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void receiveConfigChange(ConfigChangeEvent event) {
		Collection<ConfigChangeItem> changeItems = event.getChangeItems();
		if (CollectionUtils.isEmpty(changeItems)) {
			return;
		}
		for (ConfigChangeItem changeItem : changeItems) {
			if (changeItem == null) continue;
			String key = changeItem.getKey();
			if (StrUtil.isEmpty(key)) continue;
			if (key.startsWith(prefix)) {
				// 刷新ssoClientInfo
				refreshCache4ssoClientInfo();
				break;
			}
		}
	}

	/*@Override
	public void receiveConfigInfo(final String configInfo) {

	}*/

	private void refreshCache4ssoClientInfo() {
		cacheManager.getCache(CacheConstants.SSO_CLIENT_INFO).clear();
		cacheManager.getCache(CacheConstants.SSO_SERVER_TOKEN_USER_CACHE).clear();
		cacheManager.getCache(CacheConstants.SSO_SERVER_INFO).clear();
		cacheManager.getCache(CacheConstants.SSO_LOCAL_SERVER_TOKEN).clear();
		cacheManager.getCache(CacheConstants.SSO_SERVER_LOCAL_TOKEN).clear();
		cacheManager.getCache(CacheConstants.SSO_USER_SERVER_TOKEN).clear();
		cacheManager.getCache(CacheConstants.SSO_LOCAL_USER_INFO_CACHE).clear();
		cacheManager.getCache(CacheConstants.USER_DETAILS).clear();
		//Cache cache = cacheManager.getCache(CacheConstants.PROJECT_OAUTH_ACCESS);
		Set keys = redisTemplate.keys(CacheConstants.PROJECT_OAUTH_ACCESS + "*");
		if (!CollectionUtils.isEmpty(keys)) {
			redisTemplate.delete(keys);
		}
		/*List<String> keys = new ArrayList<>(Arrays.asList(ACCESS, AUTH_TO_ACCESS, AUTH, REFRESH_AUTH, ACCESS_TO_REFRESH, REFRESH, REFRESH_TO_ACCESS, CLIENT_ID_TO_ACCESS, UNAME_TO_ACCESS));
		for (String key : keys) {
			cacheManager.getCache(CacheConstants.PROJECT_OAUTH_ACCESS + key).clear();
		}*/
	}
}
