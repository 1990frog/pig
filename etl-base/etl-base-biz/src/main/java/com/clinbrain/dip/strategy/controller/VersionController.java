package com.clinbrain.dip.strategy.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.strategy.service.VersionService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (TTemplet)表控制层
 *
 * @author Liaopan
 * @since 2020-09-04 09:54:27
 */
@Api(value = "/version", tags = "版本管理")
@RestController
@RequestMapping("version")
@Slf4j
public class VersionController extends ApiBaseController {
	/**
	 * 服务对象
	 */
	@Resource
	private VersionService versionService;

	/**
	 * 分页查询所有数据
	 *
	 * @param page      分页对象
	 * @param workflowCode 查询实体
	 * @return 所有数据
	 */
	@ApiOperation(value = "查询最近几次的版本信息", notes = "分页查询最近几次的版本信息")
	@GetMapping("/page")
	public R selectAll(Page page, String workflowCode) {
		return success(this.versionService.selectVersionList(page, workflowCode));
	}




}
