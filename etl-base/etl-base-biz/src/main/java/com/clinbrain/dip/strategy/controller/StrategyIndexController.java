package com.clinbrain.dip.strategy.controller;

import com.clinbrain.dip.strategy.config.CommonConfig;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/13 0013.
 */
@Api(tags = {"etl策略"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/strategy")
@RefreshScope
public class StrategyIndexController {

	private final CommonConfig config;

	@Value("${package.path}")
	private String path;

	@GetMapping("")
	@ApiOperation(value = "验证系统状态", notes = "验证系统访问正常")
	public R index() {
		return R.ok(config.getPackagePath() + "||" + path,"hello, tactics is running ok ...");
	}

}
