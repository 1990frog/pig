package com.clinbrain.dip.strategy.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.entity.Template;
import com.clinbrain.dip.strategy.service.TemplateService;
import com.clinbrain.dip.strategy.service.VersionService;
import com.google.common.collect.Lists;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	@PostMapping("/page")
	public R selectAll(Page page, String workflowCode) {
		return success(this.versionService.selectVersionList(page, workflowCode));
	}

}
