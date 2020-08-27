package com.clinbrain.dip.tactics.controller;

import com.clinbrain.dip.tactics.config.CommonConfig;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/13 0013.
 */
@Api(tags = {"etl策略"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class IndexController {

	private final CommonConfig config;

	@GetMapping("")
	@ApiOperation(value = "验证系统状态", notes = "验证系统访问正常")
	public R index() {
		return R.ok(config.getPackagePath(),"hello, tactics is running ok ...");
	}

}
