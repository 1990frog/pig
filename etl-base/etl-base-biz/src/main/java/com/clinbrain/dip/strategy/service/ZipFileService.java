package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.strategy.config.CommonConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Liaopan on 2020-09-03.
 */
@Service
@RequiredArgsConstructor
public class ZipFileService {

	private final CommonConfig commonConfig;

	private final ModuleService moduleService;

	public void zip() {
		// 根据jobId 查询所属etl module
		System.out.println(commonConfig.getZipPassword());
	}

}
