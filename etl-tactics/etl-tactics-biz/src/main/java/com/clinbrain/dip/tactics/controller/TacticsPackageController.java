package com.clinbrain.dip.tactics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.tactics.service.TacticsService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/14 0014.
 */
@Api(tags = "策略包管理")
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class TacticsPackageController {

	private final TacticsService tacticsService;

	@GetMapping("package")
	public R listPackage(@RequestParam(required = false) Page page) {
		return R.ok(tacticsService.showFiles());
	}

}
