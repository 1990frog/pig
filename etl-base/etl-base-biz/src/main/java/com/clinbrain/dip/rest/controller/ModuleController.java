package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.metadata.DipConstants;
import com.clinbrain.dip.pojo.ETLHospital;
import com.clinbrain.dip.pojo.ETLLogSummary;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.request.RequestJson;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.workflow.ETLStart;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.pig4cloud.pig.common.core.util.R;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequestMapping("/etl/module")
@RestController
public class ModuleController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ModuleService moduleService;

	private ObjectMapper jsonMapper = new ObjectMapper();

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
     *
     * @param topicId
     * @param jobId
     * @param offset
     * @param limit
     * @param hospital
     * @param rank 排序
     * @return
     */
    @GetMapping("/names")
    public ResponseData selectAllModuleNames(@RequestParam(value = "topicId",required = false) Integer topicId,
                                        @RequestParam(value = "jobId" ,required = false) Integer jobId,
                                        @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                        @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                                        @RequestParam(value = "hospital",required = false) String hospital,
                                        @RequestParam(value = "rank",required = false,defaultValue = "desc") String rank,
                                        @RequestParam(value = "moduleName", required = false) String moduleName) {
        PageHelper.offsetPage(offset,limit);
        PageHelper.orderBy("em.created_at"+'\t'+ rank);
        Page pageData = (Page) moduleService.queryAllModules(topicId, jobId, hospital,moduleName);
        ResponseData.Page pages;
        if (pageData == null){
            pages = new ResponseData.Page(0,null);
        }else {
            pages = new ResponseData.Page(pageData.getTotal(),pageData.getResult());
        }
        return new ResponseData.Builder<ResponseData.Page>(pages).success();
    }

    @GetMapping("/check")
    public ResponseData checkModuleCode(@RequestParam String moduleCode) {
        return new ResponseData.Builder<ETLModule>(moduleService.checkModuleCode(moduleCode)).success();
    }

    /**
     * 获取任务信息信息
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
    public ResponseData updateByModule(@RequestBody ModuleTaskRequest etlModule){
        try {
            moduleService.updateModuleByCode(etlModule);
            return new ResponseData.Builder<>().success();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseData.Builder<>().error(String.format("[ %d ]任务配置修改失败",etlModule.getModuleCode()));
        }
    }

    /**
     * 修改任务优先级
     * @param moduleCode
     * @param priority
     * @return
     */
    @PostMapping("/udpatePriority")
    public ResponseData updatePriorityByModuleCode(@RequestParam("moduleCode") String moduleCode,
                                                   @RequestParam(value = "priority",defaultValue = "0") Integer priority){
        return moduleService.updatePriorityByModuleCode(moduleCode,priority) > 0 ?
             new ResponseData.Builder<>().success():
             new ResponseData.Builder<>().error("编辑优先级失败");
    }

    @PostMapping("/start")
    public ResponseData startModule(@RequestBody ModuleTaskRequest etlModule) {
        try {
            if (moduleService.updateModuleByCode(etlModule) > 0) {
                String uuid = UUID.randomUUID().toString();
                ETLStart.startByModule(etlModule.getModuleCode(), uuid);
            } else {
                return new ResponseData.Builder<>().error("提交数据失败");
            }
        } catch (Exception e) {
            logger.error("module 执行失败", e);
            return new ResponseData.Builder<>().error("任务失败,具体请查看系统日志!");
        }
        return new ResponseData.Builder<>().success();
    }

    /**
     * 启动ETL任务
     * @param moduleCode 任务标识
     * @param async 同步标识
     * @return
     */
    @GetMapping("/start/{moduleCode:.+}")
    public ResponseData startModule(@PathVariable(value = "moduleCode", required = true) String moduleCode,
                                    @RequestParam(value = "async",required = false) boolean async) {
        try {
            String uuid = UUID.randomUUID().toString();
            if (async) { // 异步方法
                moduleService.execModule(moduleCode,uuid);
                return new ResponseData.Builder<>(uuid).success();
            }else {
                ETLStart.startByModule(moduleCode, uuid);
                return new ResponseData.Builder<>(uuid).success();
            }
        } catch (Exception e) {
            logger.error("module 执行失败", e);
            return new ResponseData.Builder<>().error("执行任务失败!" + e.getMessage());
        }
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
     * @param moduleCode
     * @return
     */
    @PostMapping("clone")
    public ResponseData cloneModule(@RequestParam(required = true) String moduleCode,@RequestParam(required = true) Integer JobId) {
        try {
            ModuleTaskRequest moduleDetail = moduleService.selectModuleDetail(moduleCode);
            moduleDetail.setModuleCode(null);
            moduleDetail.setCreatedAt(new Date());
            moduleDetail.setUpdatedAt(new Date());
            //克隆后产生的名字添加clone标识
            String cloneLastModuleName = moduleDetail.getModuleName()+"_clone";
            moduleDetail.setModuleName(cloneLastModuleName);
            if (JobId > 0) {
                moduleDetail.setJobId(JobId);
            }
            moduleService.editEtlModule(moduleDetail);
        }catch (Exception e) {
            return new ResponseData.Builder<>().error(e.getMessage());
        }
        return new ResponseData.Builder<>().success();
    }

    /**
     * 修改任务所属任务组
     * @return
     */
    @PostMapping("move")
    public ResponseData moveModuleByJob(@RequestParam(required = true) String moduleCode,
                                        @RequestParam(required = true) Integer JobId){
        try {
            moduleService.moveModuleByJob(moduleCode,JobId);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ResponseData.Builder<>().error(e.getMessage());
        }
        return new ResponseData.Builder<>().success();
    }

    @GetMapping("/batch/{action}")
    public ResponseData selectDetail(@PathVariable String action,
                                     @RequestParam("moduleCode") String moduleCode) {
        if("remove".equalsIgnoreCase(action)){
            if(StringUtils.isNotEmpty(moduleCode)){
                for(String mCode : Lists.newArrayList(StringUtils.split(moduleCode,","))){
                    try {
                        moduleService.deleteModuleByModuleCode(mCode);
                    } catch (Exception e) {
                        return new ResponseData.Builder<>(e.getMessage()).error("删除出错");
                    }
                }
            }

        }
        if ("disable".equalsIgnoreCase(action) || "enable".equalsIgnoreCase(action)) {
            moduleService.batchOperation(Lists.newArrayList(StringUtils.split(moduleCode,",")),
                    "disable".equalsIgnoreCase(action));
        }

        if ("increment".equalsIgnoreCase(action) || "full".equalsIgnoreCase(action)){
            moduleService.batchFullOrIncrement(Lists.newArrayList(StringUtils.split(moduleCode,",")),
                    "increment".equalsIgnoreCase(action));
        }

        return new ResponseData.Builder<>().success();
    }

    @GetMapping("/tableEtl")
    public ResponseData selectDetail() {
        return new ResponseData.Builder<List>().data(moduleService.selectModuleCodeByWorkflowInfo()).success();
    }

}