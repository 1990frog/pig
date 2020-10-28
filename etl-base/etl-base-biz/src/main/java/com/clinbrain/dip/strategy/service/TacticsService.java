package com.clinbrain.dip.strategy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.rest.service.ConnectionService;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.config.CommonConfig;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.strategy.util.ServiceUtil;
import com.pig4cloud.pig.common.core.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2020/8/14 0014.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TacticsService {

	private final CommonConfig config;

	private final TemplateMapper templateMapper;

	private final ConnectionService dataBaseService;

	private final CacheableService cacheableService;

	private final ZipFileService zipFileService;

	private final TemplateService templateService;


	public IPage<String> showFiles() {
		log.debug("本地策略包存放路径：" + config.getPackagePath());
		zipFileService.zip();
		final List<PackageInfo> packageInfos = cacheableService.packageInfos();
		List<ETLConnection> connections = dataBaseService.selectAll();
		System.out.println(connections);
		return new Page().setRecords(packageInfos);
	}

	public List<String> vendorList() {
		final List<PackageInfo> packageInfos = cacheableService.packageInfos();
		return Optional.ofNullable(packageInfos).orElse(Collections.emptyList())
			.stream().map(PackageInfo::getVendor).collect(Collectors.toList());
	}

}