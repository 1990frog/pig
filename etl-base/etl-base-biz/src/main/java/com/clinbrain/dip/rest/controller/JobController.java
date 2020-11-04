package com.clinbrain.dip.rest.controller;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.clinbrain.dip.connection.ClientOutput;
import com.clinbrain.dip.metadata.azkaban.Project;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLLogDetail;
import com.clinbrain.dip.pojo.ETLLogSummary;
import com.clinbrain.dip.pojo.ETLScheduler;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.AzkabanJobManageService;
import com.clinbrain.dip.rest.service.EtlSchedulerService;
import com.clinbrain.dip.rest.service.JobService;
import com.clinbrain.dip.strategy.bean.ETLSchedulerDto;
import com.clinbrain.dip.strategy.util.CronUtil;
import com.clinbrain.dip.util.ProcessUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/etl/job")
@Api("job类")
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    @Autowired
    JobService jobService;

    @Autowired
    AzkabanJobManageService azkabanJobManageService;

    @Autowired
    EtlSchedulerService schedulerService;

    /**
     * 任务日志
     * @param jobName 任务组名称
     * @return
     */
    @GetMapping("/logsummary/{jobName}")
    public ResponseData listJobByName(@PathVariable("jobName") String jobName,
                                      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                      @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit){
        try {
            PageHelper.offsetPage(offset,limit);
            List<ETLJob> jobs = jobService.listJobByName(jobName);
            return new ResponseData.Builder<List>(jobs).success();
        }catch (Exception e){
            e.getMessage();
            return new ResponseData.Builder<>().error(e.getMessage());
        }
    }

    /**
     * 任务组列表
	 * 2020.10.28 修改。job中的enable字段现在用来标识是否发布到azkaban上
     * @param id 业务类型id
     * @param rank 排序
     * @return
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public R getAllEngines(@RequestParam(value = "topId", required = false) Integer id,
						   @RequestParam(value = "jobName", required = false) String name,
						   @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
						   @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
						   @RequestParam(value = "rank",required = false, defaultValue = "DESC") String rank) {
        try {
			PageHelper.offsetPage(offset,limit);
			PageHelper.offsetPage(offset,limit);
			PageHelper.orderBy("updated_at "+rank);
			Page pageData = (Page)jobService.getJobs(id,name);
			ResponseData.Page<ETLJob> pages = new ResponseData.Page<ETLJob>(pageData.getTotal(), pageData.getResult());


			List<ETLJob> jobs = jobService.getJobs(id, name);
			if(pages != null && !pages.getRows().isEmpty()) {
				pages.getRows().stream().peek(job -> {
					try {
						final Boolean existProject = azkabanJobManageService.isExistProject(new Project(job.getJobName(), "", ""));
						job.setEnabled(existProject?1:0);
					}catch (Exception e) {
						job.setEnabled(0);
					}
				});
			}

			return R.ok(pages);
        } catch (Exception e) {
            logger.error("查询job出错",e);
            return R.failed("查询JOB失败");
        }

    }

    @PostMapping("/edit")
    public ResponseData edit(@RequestBody ETLJob jobInfo) {
        return new ResponseData.Builder<ETLJob>(jobService.createOrUpdate(jobInfo)).success();
    }

    /**
     * 任务上传到调度系统
     * @param jobInfo 任务信息
     * @return
     */
    @PostMapping("/upload")
    public ResponseData upload(@RequestBody ETLJob jobInfo) {
        try {
            return jobService.upload(jobInfo);
        } catch (Exception e) {
            logger.error("upload jobInfo Name :" + jobInfo.getJobName() + " failed.\nError message: \n" + e);
            return new ResponseData.Builder<>().data("").error("创建失败:" + e.getMessage());
        }
    }

    /**
     * 验证调度时间表达式
     * @param cronExpression 时间表达式
     * @return
     */
    @GetMapping("/cronvalid")
    public ResponseData validCronExp(@RequestParam("cron") String cronExpression) {
        if (StringUtils.isEmpty(cronExpression)) {
            return new ResponseData.Builder<Boolean>().data(false).success();
        }
        return new ResponseData.Builder<Boolean>().data(CronExpression.isValidExpression(cronExpression)).success();
    }

    /**
     *
     * @param jobId
     * @param offset
     * @param limit
     * @param moduleCode
     * @param status
     * @param hospital
     * @return
     */
    @GetMapping("/logsummary")
    public ResponseData showLogs(@RequestParam(value = "jobId", required = false) Integer jobId,
                                 @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
                                 @RequestParam(value = "limit", defaultValue = "10", required = false) int limit,
                                 @RequestParam(value = "moduleCode",required = false) String moduleCode,
                                 @RequestParam(value = "status",defaultValue = "0") Integer status,
                                 @RequestParam(value = "hospital", required = false) String hospital) {
        return new ResponseData.Builder<>().data(jobService.selectLogSummarys(offset, limit, jobId,moduleCode,status,hospital)).success();
    }

    @PostMapping("/updatelogsummary")
    public ResponseData updateLogsummaryEndDateBySummaryId(@RequestBody ETLLogSummary summary) {
        jobService.updateLogsummaryEndDateBySummaryId(summary.getEndDate(),summary.getSummaryId());
        return new ResponseData.Builder<>().success();
    }


    /**
     * 查看具体任务详细组件日志信息
     * @param summaryId 日志总体ID
     * @return
     */
    @GetMapping("/{summaryId}/logdetail")
    public ResponseData showLogs(@PathVariable("summaryId") Integer summaryId) {
        try {
            List<ETLLogDetail> details = jobService.selectLogDetailsBySummaryId(summaryId);
            return new ResponseData.Builder<>().data(details).success();
        }catch (Exception e){
            return new ResponseData.Builder<>().error(e.getMessage());
        }

    }

    /**
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public ResponseData deleteEtlJob(@PathVariable("id") Integer id) {
        if (id < 0) {
            return new ResponseData.Builder<Boolean>().data(false).error("错误");
        }
        try {
            jobService.deleteJobById(id);
            return new ResponseData.Builder<Boolean>(true).success();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseData.Builder<Boolean>().data(false).error(e.getMessage());
        }
    }

    @PostMapping
    public ResponseData checkJobName(@RequestParam(required = false) String jobName) {
        if (StringUtils.isEmpty(jobName)) {
            return new ResponseData.Builder<>().success(null);
        }
        ETLJob job = jobService.checkJobName(jobName);
        if (job != null)
            return new ResponseData.Builder<>().error(String.format("工作名称[ %s ]已经存在", jobName));
        return new ResponseData.Builder<>().success();
    }

    /**
     * 获取调度时间列表
     */
    @GetMapping("/scheduler/all")
    public ResponseData selectAllScheduler() {
        return new ResponseData.Builder<List>().data(schedulerService.selectAll()).success();
    }

    @PostMapping("/scheduler/save")
    public ResponseData selectAllScheduler(@RequestBody ETLSchedulerDto scheduler) {
		scheduler.setSchedulerCron(CronUtil.createCronExpression(scheduler.getCromModel()));
		scheduler.setSchedulerJson(JSON.toJSONString(scheduler.getCromModel()));
		ETLScheduler newScheduler = new ETLScheduler();
		BeanUtil.copyProperties(scheduler, newScheduler);

        if (scheduler.getSchedulerId()!=null) {
            try {
                scheduler.setUpdatedAt(new Date());

                schedulerService.updateByPrimaryKey(newScheduler);
                return new  ResponseData.Builder<Boolean>().success();
            }catch (Exception e){
                logger.error(e.getMessage());
                return new ResponseData.Builder<Boolean>().error(e.getMessage());
            }
        }
        if (scheduler.getCreatedAt()==null || scheduler.getUpdatedAt()==null){
			newScheduler.setCreatedAt(new Date());
			newScheduler.setUpdatedAt(new Date());
        }
        int i = schedulerService.insert(newScheduler);
        return i > 0 ? new ResponseData.Builder<Boolean>().success() : new ResponseData.Builder<Boolean>().error("保存失败");
    }

    @DeleteMapping("/scheduler/{id}")
    public ResponseData deleteSchedule(@PathVariable("id") Integer id){
        ETLScheduler scheduler = new ETLScheduler();
        scheduler.setSchedulerId(id);
        return new ResponseData.Builder<>(schedulerService.deleteByPrimaryKey(scheduler)).success();
    }

    @GetMapping("/killdatax")
    public ResponseData killDataxProcessor(@RequestParam(value = "uidName") String uidName,
                                           @RequestParam(value = "sidName") Integer sidName,
                                           @RequestParam(value = "detailId") Integer detailId){
        ClientOutput clientOutput = ProcessUtil.killDataxProcess(uidName);
        if(clientOutput.getExitCode() == 0){
            jobService.updateLogsummaryAndLogdetails(sidName,detailId);
            return new ResponseData.Builder<>().success();
        }
        return new ResponseData.Builder<>().error(clientOutput.getText());
    }

    @GetMapping("logs")
    public ResponseData realTimePrintLogs(@RequestParam String ip, @RequestParam String logPth){
        StringBuffer buffer = null;
        try {
            buffer = jobService.realTimePrintLogs(ip,logPth);
        } catch (Exception e) {
            return new ResponseData.Builder<>().error(e.getMessage());
        }
        return new ResponseData.Builder<>(buffer).success();
    }



	/**
	 * 任务组列表数
	 * @param topId 业务类型id
	 * @return
	 */
	@ApiOperation("job查询任务树")
	@RequestMapping(value = "/allTree", method = RequestMethod.GET)
	public R selectJobTree(@RequestParam(value = "topId", required = false) Integer topId,  @RequestParam(value = "jobName", required = false) String name,@RequestParam(value = "moduleName", required = false) String moduleName) {
		try {
			return R.ok(jobService.selectJobTree(topId,name, moduleName));
		} catch (Exception e) {
			logger.error("查询job出错",e);
			return R.failed("查询JOB失败");
		}

	}
}
