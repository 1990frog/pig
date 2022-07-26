package com.pig4cloud.pig.common.sso.component;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.pig4cloud.pig.common.sso.listener.SSOConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @ClassName SSONacosPropertySource
 * @Author Duys
 * @Description
 * @Date 2022/7/26 13:03
 **/
@Component
@Slf4j
public class SSONacosPropertySource {

	private static final String SEP1 = "-";

	private static final String DOT = ".";

	//@Autowired
	//private NacosPropertySourceLocator nacosPropertySourceLocator;

	@Autowired
	private NacosConfigManager nacosConfigManager;

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	@Autowired
	private Environment environment;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private RedisTemplate redisTemplate;

	@Value("${spring.application.name:pig-gateway}")
	private String dataIdPrefix;

	@PostConstruct
	public void addSsoPropertyListener() {
		ConfigService configService = nacosConfigManager.getConfigService();
		String name = nacosConfigProperties.getName();
		String nacosGroup = nacosConfigProperties.getGroup();

		String dataId = nacosConfigProperties.getPrefix();
		if (StrUtil.isEmpty(dataId)) {
			dataId = name;
		}
		if (StrUtil.isEmpty(dataId)) {
			dataId = dataIdPrefix;
		}
		String fileExtension = nacosConfigProperties.getFileExtension();
		try {
			// 注册感兴趣的事情
			for (String profile : environment.getActiveProfiles()) {
				dataId = dataId + SEP1 + profile + DOT + fileExtension;
				configService.addListener(dataId, nacosGroup, new SSOConfigListener(cacheManager,redisTemplate));
			}
		} catch (Exception e) {
			log.error("注册环境变量的监听异常！ error = {}", e);
		}
	}
}
