package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.config.CommonConfig;
import com.clinbrain.dip.strategy.constant.TacticsConstant;
import com.clinbrain.dip.strategy.util.PackageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Liaopan on 2020/8/19 0019.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheableService {

	private final CommonConfig config;

	private final CacheManager cacheManager;

	@Cacheable(cacheManager = "redis",key = "'packageAll'", cacheNames = TacticsConstant.PACKAGE_REDIS_KEY, unless = "#result == null")
	public List<PackageInfo> packageInfos() {
		log.debug("从本地["+config.getPackagePath()+"]加载策略包信息");
		List<PackageInfo> packageInfos = PackageUtil.fetchPackageInfos(config.getPackagePath());
		return packageInfos;
	}
}
