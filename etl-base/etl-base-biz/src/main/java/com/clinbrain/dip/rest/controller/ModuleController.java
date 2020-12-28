package com.clinbrain.dip.rest.controller;

import cn.hutool.db.Entity;
import cn.hutool.db.PageResult;
import com.clinbrain.dip.metadata.DipConstants;
import com.clinbrain.dip.pojo.ETLHospital;
import com.clinbrain.dip.pojo.ETLLogSummary;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.request.RequestJson;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.strategy.controller.ApiBaseController;
import com.clinbrain.dip.workflow.ETLStart;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Api(tags = {"etl具体任务（Module）"})
@RequestMapping("/etl/module")
@RestController
public class ModuleController extends ApiBaseController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ModuleService moduleService;

    @GetMapping("")
    public ResponseData selectAllModules() {
        return new ResponseData.Builder<>(moduleService.selectAll()).success();
    }

	@GetMapping("/{moduleCode}/info")
	public ResponseData selectOne(@PathVariable String moduleCode) {
		logger.debug(moduleCode);
		return new ResponseData.Builder<ETLModule>(moduleService.selectModuleDetailByCode(moduleCode)).success();
	}

    /**
     *
     * @param topicId
     * @param jobId
     * @param hospital
     * @param rank 排序
     * @return
     */
    @GetMapping("/all")
    public R selectAllModule(@RequestParam(value = "topicId",required = false) Integer topicId,
							 @RequestParam(value = "jobId" ,required = false) Integer jobId,
							 @RequestParam(value = "hospital",required = false) String hospital,
							 @RequestParam(value = "rank",required = false,defaultValue = "desc") String rank,
							 @RequestParam(value = "moduleName", required = false) String moduleName) {
        return R.ok(moduleService.queryModuleDetails(topicId, jobId, hospital,moduleName));
    }

	/**
	 * @param topicId
	 * @param jobId
	 * @param offset
	 * @param limit
	 * @param hospital
	 * @param rank     排序
	 * @return
	 */
	@GetMapping("/names")
	public ResponseData selectAllModuleNames(@RequestParam(value = "topicId", required = false) Integer topicId,
											 @RequestParam(value = "jobId", required = false) Integer jobId,
											 @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
											 @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
											 @RequestParam(value = "hospital", required = false) String hospital,
											 @RequestParam(value = "rank", required = false, defaultValue = "desc") String rank,
											 @RequestParam(value = "moduleName", required = false) String moduleName) {
		PageHelper.offsetPage(offset, limit);
		PageHelper.orderBy("em.created_at" + '\t' + rank);
		Page pageData = (Page) moduleService.queryAllModules(topicId, jobId, hospital, moduleName);
		ResponseData.Page pages;
		if (pageData == null) {
			pages = new ResponseData.Page(0, null);
		} else {
			pages = new ResponseData.Page(pageData.getTotal(), pageData.getResult());
		}
		return new ResponseData.Builder<ResponseData.Page>(pages).success();
	}

	@GetMapping("/check")
	public ResponseData checkModuleCode(@RequestParam String moduleCode) {
		return new ResponseData.Builder<ETLModule>(moduleService.checkModuleCode(moduleCode)).success();
	}

	/**
	 * 获取任务信息信息
	 *
	 * @param moduleCode 任务标识
	 * @return
	 */
	@GetMapping("/{moduleCode}/jsoninfo")
	public ResponseData selectDetail(@PathVariable String moduleCode) {
		ModuleTaskRequest moduleTaskRequest = moduleService.selectModuleDetail(moduleCode);
		return new ResponseData.Builder<>(moduleTaskRequest).success();
	}

    @PostMapping("")
    public ResponseData edit(@RequestJson(value = "") ModuleTaskRequest moduleTask) {
        try {
            moduleService.editEtlModule(moduleTask);
        } catch (Exception e) {
            logger.error("编辑module出错:", e);
            return new ResponseData.Builder<>().error(e.getMessage());
        }
        return new ResponseData.Builder<>().success();
    }

	@PutMapping("{code:.+}")
	public ResponseData renovateModuleStatus(@PathVariable("code") String code, @RequestParam Integer enabled) {
		try {
			moduleService.renovateModuleStatus(code, enabled);
			return new ResponseData.Builder<>().success();
		} catch (Exception e) {
			logger.error("module禁用失败", e);
			return new ResponseData.Builder<>().error(e.getMessage());
		}
	}

	@PostMapping("/update")
	public ResponseData updateByModule(@RequestBody ModuleTaskRequest etlModule) {
		try {
			moduleService.updateModuleByCode(etlModule);
			return new ResponseData.Builder<>().success();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseData.Builder<>().error(String.format("[ %d ]任务配置修改失败", etlModule.getModuleCode()));
		}
	}

	/**
	 * 修改任务优先级
	 *
	 * @param moduleCode
	 * @param priority
	 * @return
	 */
	@PostMapping("/udpatePriority")
	public ResponseData updatePriorityByModuleCode(@RequestParam("moduleCode") String moduleCode,
												   @RequestParam(value = "priority", defaultValue = "0") Integer priority) {
		return moduleService.updatePriorityByModuleCode(moduleCode, priority) > 0 ?
			new ResponseData.Builder<>().success() :
			new ResponseData.Builder<>().error("编辑优先级失败");
	}

	/**
	 * 修改任务依赖（新的优先级编辑）
	 *
	 * @param moduleCode 当前任务编码
	 * @param dependencyCode 依赖的任务编码
	 * @return
	 */
	@ApiOperation("设置当前任务的依赖任务")
	@PutMapping("/dependency")
	public ResponseData updatePriorityByModuleCode(@ApiParam("当前任务code")@RequestParam("moduleCode") String moduleCode,
												   @ApiParam("依赖任务code")@RequestParam(value = "dependencyCode") String dependencyCode) {
		if(StringUtils.equalsIgnoreCase(moduleCode, dependencyCode)) {
			return new ResponseData.Builder<>().error("任务不能循环依赖");
		}
		return moduleService.editPriorityByModuleCode(moduleCode, dependencyCode) > 0 ?
			new ResponseData.Builder<>().success() :
			new ResponseData.Builder<>().error("编辑任务依赖失败");
	}

	/**
	 * 查找任务依赖节点列表
	 *
	 * @param jobId 当前任务所属任务组ID
	 * @return
	 */
	@ApiOperation("查询当前任务的依赖列表")
	@GetMapping("/dependencyList")
	public ResponseData dependencyList(@ApiParam("任务组ID") @RequestParam("jobId") Integer jobId) {
			return new ResponseData.Builder<>(moduleService.dependencyList(jobId)).success();
	}

	@ApiOperation("运行任务，同时保存任务参数，如区间/增量模式，区间时间设置")
	@PostMapping("/start")
	public ResponseData startModule(@RequestBody ModuleTaskRequest etlModule) {
		try {
			if (moduleService.updateModuleByCode(etlModule) > 0) {
				String uuid = UUID.randomUUID().toString();
				moduleService.execModule(etlModule.getModuleCode(), uuid);
				return new ResponseData.Builder<>(uuid).success();
			} else {
				return new ResponseData.Builder<>().error("提交数据失败");
			}
		} catch (Exception e) {
			logger.error("module 执行失败", e);
			return new ResponseData.Builder<>().error("任务失败,具体请查看系统日志!");
		}
	}

	/**
	 * 启动ETL任务
	 *
	 * @param moduleCode 任务标识
	 * @param async      同步标识
	 * @return
	 */
	@GetMapping("/start/{moduleCode:.+}")
	public ResponseData startModule(@PathVariable(value = "moduleCode", required = true) String moduleCode,
									@RequestParam(value = "async", required = false) boolean async) {
		try {
			String uuid = UUID.randomUUID().toString();
			if (async) { // 异步方法
				moduleService.execModule(moduleCode, uuid);
				return new ResponseData.Builder<>(uuid).success();
			} else {
				ETLStart.startByModule(moduleCode, uuid);
				return new ResponseData.Builder<>(uuid).success();
			}
		} catch (Exception e) {
			logger.error("module 执行失败", e);
			return new ResponseData.Builder<>().error("执行任务失败!" + e.getMessage());
		}
	}

	@ApiOperation("数据核查预览")
	@GetMapping("/preCheck/{moduleCode:.+}")
	public ResponseData startCheckData(@PathVariable(value = "moduleCode") String moduleCode,
									   @RequestParam("workflowCode") @ApiParam("核查点对应的组件code") String workflowCode,
									   @RequestParam("startTime") String startTime,
									   @RequestParam("endTime") String endTime,
									   @RequestParam("page") Integer pageSize, @RequestParam("num") Integer pageNumber) {
		String uuid = UUID.randomUUID().toString();
		try {
			 moduleService.execCheckDataModule(moduleCode,workflowCode,startTime,endTime,uuid, new cn.hutool.db.Page(pageNumber, pageSize));
//			Map<String,Object> resultMap = new HashMap<>();
//			resultMap.put("total", result.getTotal());
//			resultMap.put("rows", result);
			Map<String, Object> paramMap = new HashMap<>();
			// 编辑核查点
			if(moduleService.editWorkflowPoint(workflowCode) > 0) {
				moduleService.execModule(moduleCode, uuid, paramMap);
			}

			return new ResponseData.Builder<>(uuid).success(uuid);
		} catch (Exception e) {
			logger.error("核查执行失败", e);
			return new ResponseData.Builder<>(uuid).success(uuid);
		}
	}

	@ApiOperation("数据核查预览后的表格数据")
	@GetMapping("/preCheck/pageData/{moduleCode:.+}")
	public ResponseData getPageData(@PathVariable(value = "moduleCode") String moduleCode,
									@RequestParam("page") Integer pageSize, @RequestParam("num") Integer pageNumber) {
		try {
			final PageResult<Entity> result = moduleService.getList(moduleCode, new cn.hutool.db.Page(pageNumber, pageSize));
			Map<String,Object> resultMap = new HashMap<>();
			resultMap.put("total", result.getTotal());
			resultMap.put("rows", result);

			return new ResponseData.Builder<>(resultMap).success();
		} catch (Exception e) {
			logger.error("获取分页数据失败", e);
			return new ResponseData.Builder<>().error("获取分页数据失败!" + e.getMessage());
		}
	}

	@ApiOperation("数据核查")
	@GetMapping("/startCheck")
	public ResponseData startCheck(@RequestParam("moduleCode") @ApiParam("任务Code") String moduleCode,
								   @RequestParam("startTime") @ApiParam("核查开始时间")  String startTime,
								   @RequestParam("endTime")  @ApiParam("核查结束时间") String endTime,
								   @RequestParam("uuid") String uuid) {

		try {
			final ETLLogSummary logSummary = moduleService.startCheck(moduleCode, startTime, endTime, uuid);
			return new ResponseData.Builder<>(logSummary).success();
		} catch (Exception e) {
			logger.error("核查失败");
			return new ResponseData.Builder<>().error("调用数据核查服务出错!" + e.getMessage());
		}
	}

	@ApiOperation("数据核查结果展示")
	@GetMapping("/checkResult")
	public ResponseData checkResult(@RequestParam("checkId") String uuid) {
		try {
			final String checkId = moduleService.checkResult(uuid);
			return new ResponseData.Builder<>(checkId).success();
		} catch (Exception e) {
			logger.error("获取核查结果失败");
			return new ResponseData.Builder<>().error("获取核查结果失败!" + e.getMessage());
		}
	}

	@ApiOperation("核查结果明细")
	@GetMapping("/checkReport")
	public ResponseData getCheckResult(@RequestParam("batchno") String batchno) {
			return new ResponseData.Builder<>(moduleService.checkReport(batchno)).success();
	}

	@GetMapping("/taskType")
	public ResponseData getModuleType() {
		try {
			DipConstants.ModuleCategory[] values = DipConstants.ModuleCategory.values();
			List<String> list = Lists.newArrayList();
			for (int i = 0; i < values.length; i++) {
				list.add(values[i].toString());
			}
			return new ResponseData.Builder<>(list).success();
		} catch (Exception e) {
			return new ResponseData.Builder<>().error(e.getMessage());
		}
	}

	@GetMapping("/logs")
	public ResponseData getLogsInfoByModule(@RequestParam String module, @RequestParam(required = false) String runtime) {
		if (StringUtils.isBlank(module)) {
			return new ResponseData.Builder<>(null).error(String.format("module %s 没有获取到", module));
		}
		try {
			ETLLogSummary summary = moduleService.getLogsInfoByModule(module, runtime);
			if (summary != null) {
				return new ResponseData.Builder<ETLLogSummary>(summary).success();
			}
			return new ResponseData.Builder<>(null).error("查询LOG信息失败");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return new ResponseData.Builder<>(null).error(e.getMessage());
		}
	}

	@DeleteMapping(value = "/{code:.+}")
	public ResponseData cancelModuleByModuleCode(@PathVariable("code") String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		try {
			int JobId = moduleService.deleteModuleByModuleCode(code);
			return new ResponseData.Builder<>().data(JobId).success();
		} catch (Exception e) {
			return new ResponseData.Builder<>().error(e.getMessage());
		}
	}

	@GetMapping(value = "/hospital")
	public ResponseData queryHospitals() {
		try {
			return new ResponseData.Builder<>(moduleService.queryHospitals()).success();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return new ResponseData.Builder<>().error(e.getMessage());
		}
	}

	@PostMapping(value = "/hospital/edit")
	public ResponseData editEtlHospital(@RequestBody ETLHospital hospital) {
		if (StringUtils.isAnyBlank(hospital.getHospitalCode(), hospital.getHospitalName())) {
			return new ResponseData.Builder<>().error("缺少必要的字段");
		}
		try {
			if (moduleService.editEtlHospital(hospital) > 0) {
				return new ResponseData.Builder<>().success();
			}
			return new ResponseData.Builder<>().error("失败");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return new ResponseData.Builder<>().error(e.getMessage());
		}
	}

	@DeleteMapping(value = "hosp/{code}")
	public ResponseData removeHospitalByCode(@PathVariable("code") String code) {
		moduleService.removeHospitalByCode(code);
		return new ResponseData.Builder<>().success();
	}

	/**
	 * 克隆任务
	 *
	 * @param moduleCode
	 * @return
	 */
	@PostMapping("clone")
	public ResponseData cloneModule(@RequestParam(required = true) String moduleCode, @RequestParam(required = true) Integer JobId) {
		try {
			ModuleTaskRequest moduleDetail = moduleService.selectModuleDetail(moduleCode);
			moduleDetail.setModuleCode(null);
			moduleDetail.setCreatedAt(new Date());
			moduleDetail.setUpdatedAt(new Date());
			//克隆后产生的名字添加clone标识
			String cloneLastModuleName = moduleDetail.getModuleName() + "_clone";
			moduleDetail.setModuleName(cloneLastModuleName);
			if (JobId > 0) {
				moduleDetail.setJobId(JobId);
			}
			moduleService.editEtlModule(moduleDetail);
		} catch (Exception e) {
			return new ResponseData.Builder<>().error(e.getMessage());
		}
		return new ResponseData.Builder<>().success();
	}

	/**
	 * 修改任务所属任务组
	 *
	 * @return
	 */
	@PostMapping("move")
	public ResponseData moveModuleByJob(@RequestParam(required = true) String moduleCode,
										@RequestParam(required = true) Integer JobId) {
		try {
			moduleService.moveModuleByJob(moduleCode, JobId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseData.Builder<>().error(e.getMessage());
		}
		return new ResponseData.Builder<>().success();
	}

	@GetMapping("/batch/{action}")
	public ResponseData selectDetail(@PathVariable String action,
									 @RequestParam("moduleCode") String moduleCode) {
		if ("remove".equalsIgnoreCase(action)) {
			if (StringUtils.isNotEmpty(moduleCode)) {
				for (String mCode : Lists.newArrayList(StringUtils.split(moduleCode, ","))) {
					try {
						moduleService.deleteModuleByModuleCode(mCode);
					} catch (Exception e) {
						return new ResponseData.Builder<>(e.getMessage()).error("删除出错");
					}
				}
			}

		}
		if ("disable".equalsIgnoreCase(action) || "enable".equalsIgnoreCase(action)) {
			moduleService.batchOperation(Lists.newArrayList(StringUtils.split(moduleCode, ",")),
				"disable".equalsIgnoreCase(action));
		}

		if ("increment".equalsIgnoreCase(action) || "full".equalsIgnoreCase(action)) {
			moduleService.batchFullOrIncrement(Lists.newArrayList(StringUtils.split(moduleCode, ",")),
				"increment".equalsIgnoreCase(action));
		}

		return new ResponseData.Builder<>().success();
	}

	@GetMapping("/tableEtl")
	public ResponseData selectDetail() {
		return new ResponseData.Builder<List>().data(moduleService.selectModuleCodeByWorkflowInfo()).success();
	}

	/**
	 * 根据moduleCode查询下属 workflows 的运行状态
	 * @param moduleCode
	 * @return
	 */
	@ApiOperation("根据任务编码获取组件最近运行状态")
	@GetMapping("/workflow/status")
	public R moduleWorkflowStatus(@RequestParam("moduleCode") String moduleCode,@RequestParam("uuid") String uuid) {
		return success(moduleService.selectWorkflowStatus(moduleCode, uuid));
	}

	@ApiOperation("根据任务运行的uuid 获取运行日志")
	@GetMapping("/tailLog")
	public R tailLogs(@RequestParam("uid") String uuid) {
		try {
			final Pair<ETLLogSummary, String> etlLogSummaryStringPair = moduleService.tailLog(uuid);
			return success(etlLogSummaryStringPair.getLeft(), etlLogSummaryStringPair.getRight());
		}catch (Exception e) {
			logger.error("获取运行记录出错！", e);
			return failed(e.getMessage());
		}
	}

}
