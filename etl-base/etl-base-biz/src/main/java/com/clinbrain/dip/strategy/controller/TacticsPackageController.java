package com.clinbrain.dip.strategy.controller;

import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.service.TacticsService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import oracle.ucp.proxy.annotation.Post;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Liaopan on 2020/8/14.
 * 包管理： 压缩包列表，查看，加解密，解压缩等
 */
@Api(tags = "策略包管理")
@RestController
@RequestMapping("package")
@RequiredArgsConstructor
public class TacticsPackageController extends ApiBaseController {

	private final TacticsService tacticsService;

	/**
	 * 展示所有的任务包
	 *
	 * @param page
	 * @return
	 */
	@ApiOperation("任务列表展示（策略包）")
	@PostMapping("list")
	public R listPackage(@MultiRequestBody(required = false) PageParam page) {
		return R.ok(tacticsService.showFiles());
	}

	@ApiOperation("模板匹配")
	@PostMapping("matching")
	public R matchingTemplate(@MultiRequestBody(value = "codes") String templateCodes,
							  @MultiRequestBody(value = "conns") List<SystemConnectionCodeVO> voList) {
		// 选择的模板code
		System.out.println(templateCodes);
		// 本地的连接code, 和连接
		System.out.println(voList.size());
		voList.forEach(System.out::println);
		return R.ok();
	}

	@ApiOperation("模板匹配")
	@PostMapping("matching2")
	public R matchingTemplate2(@MultiRequestBody(value = "codes") String templateCodes,
							  @MultiRequestBody(value = "conns") List voList) {
		// 选择的模板code
		System.out.println(templateCodes);
		// 本地的连接code, 和连接
		System.out.println(voList.size());
		voList.forEach(System.out::println);
		return R.ok();
	}
}
