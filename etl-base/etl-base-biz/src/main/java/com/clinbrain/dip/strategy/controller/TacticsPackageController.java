package com.clinbrain.dip.strategy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.strategy.service.TacticsService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/14.
 *  包管理： 压缩包列表，查看，加解密，解压缩等
 */
@Api(tags = "策略包管理")
@RestController
@RequestMapping("package")
@RequiredArgsConstructor
public class TacticsPackageController {

	private final TacticsService tacticsService;

	@GetMapping("list")
	public R listPackage(@RequestParam(required = false) Page page) {
		return R.ok(tacticsService.showFiles());
	}

}
