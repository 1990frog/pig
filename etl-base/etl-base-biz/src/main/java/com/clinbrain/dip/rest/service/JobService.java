package com.clinbrain.dip.rest.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.file.FileReader;
import com.clinbrain.dip.common.DipConfig;
import com.clinbrain.dip.metadata.azkaban.Project;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLLogDetail;
import com.clinbrain.dip.pojo.ETLLogSummary;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLScheduler;
import com.clinbrain.dip.pojo.ETLTopic;
import com.clinbrain.dip.pojo.EtlJobModule;
import com.clinbrain.dip.rest.bean.EtlJobVersion;
import com.clinbrain.dip.rest.mapper.DBETLJobMapper;
import com.clinbrain.dip.rest.mapper.DBETLJobModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLLogSummaryMapper;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLSchedulerMapper;
import com.clinbrain.dip.rest.mapper.DBETLTopicMapper;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.util.CreateZipFile;
import com.clinbrain.dip.rest.vo.ETLJobVo;
import com.clinbrain.dip.rest.vo.ModuleCheckStatus;
import com.clinbrain.dip.strategy.entity.Template;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.util.FtpHelper;
import com.clinbrain.dip.util.SftpHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService extends BaseService<ETLJob> {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    @Autowired
    private CreateZipFile createZipFile;

    @Autowired
    @Qualifier("logSummaryMapper")
    private DBETLLogSummaryMapper logSummaryMapper;

    @Autowired
    private DBETLModuleMapper moduleMapper;

    @Autowired
    private AzkabanJobManageService azkabanJobManageService;

    @Autowired
    @Qualifier("jobModuleMapper")
    private DBETLJobModuleMapper jobModuleMapper;

    @Autowired
    private DBETLTopicMapper topicMapper;

    @Autowired
    @Qualifier("jobMapper")
    private DBETLJobMapper jobMapper;

    @Autowired
    @Qualifier("schedulerMapper")
    private DBETLSchedulerMapper schedulerMapper;

    private String zipFilePath = System.getProperty("java.io.tmpdir");

    public ETLJob createOrUpdate(ETLJob job) {
        job.setUpdatedAt(new Date());
        if (job.getId() == null) {
            job.setCreatedAt(new Date());
            mapper.insert(job);
        } else {
            mapper.updateByPrimaryKey(job);
        }
        return job;
    }

    /**
     *  上传job-scheduler 到 azkb上
     *  Optional.ofNullable(job).map(ETLJob::getScheduler).map(ETLScheduler::getSchedulerCron).orElse(null)
     *  判断job -> scheduler -> cron 是否为空
     * @param job
     * @return
     * @throws Exception
     */
    public ResponseData<String> upload(Integer jobId) throws Exception{
        // 重新查找job信息
        ETLJob job = jobMapper.selectByPrimaryKey(jobId);
        ETLTopic etlTopic = topicMapper.selectByPrimaryKey(Integer.valueOf(job.getTopicId()));
        job.setScheduler(schedulerMapper.selectByPrimaryKey(job.getSchedulerId()));

        Project project = new Project(job.getJobName(),job.getJobName(),
                Optional.ofNullable(etlTopic.getTopicName()).orElse("未设置"),"create");
        String cronExp = Optional.ofNullable(job).map(ETLJob::getScheduler).map(ETLScheduler::getSchedulerCron).orElse(null);
        if(StringUtils.isEmpty(cronExp)){
            return new ResponseData.Builder("").error("该任务还未设置执行计划！");
        }
        project.setCronExpression(cronExp);
        //String filePath = createZipFile.createJobFileByJobId(job.getId());
        String filePath = createZipFile.createJobFileById(job.getId());
        String zipName = createZipFile.createZipByJobName(filePath, UUID.randomUUID().toString());
        if(StringUtils.isEmpty(zipName)){
            return new ResponseData.Builder("").error("任务上传失败，请检查该job下任务是否启用或者任务的依赖配置错误！");
        }else {
            project.setZipFileName(zipName);
            ResponseData<String> responseData = azkabanJobManageService.createOrUpdateProject(project);
            createZipFile.deleteFile(zipFilePath + File.separator + zipName);
            return responseData;
        }
    }

	/**
	 * 发布信息时指定etl任务code
	 * @param jobId
	 * @param modules
	 * @return
	 * @throws Exception
	 */
	public ResponseData<String> uploadByModules(Integer jobId, List<ETLModule> modules) throws Exception{

		// 先更新module信息
		for(ETLModule module : modules) {
			moduleMapper.updateModuleByCode(module);
		}

		// 重新查找job信息，作为发布到azkaban上的项目信息
		ETLJob job = jobMapper.selectByPrimaryKey(jobId);
		ETLTopic etlTopic = topicMapper.selectByPrimaryKey(Integer.valueOf(job.getTopicId()));
		job.setScheduler(schedulerMapper.selectByPrimaryKey(job.getSchedulerId()));

		Project project = new Project(job.getJobName(),job.getJobName(),
				Optional.ofNullable(etlTopic.getTopicName()).orElse("未设置"),"create");
		String cronExp = Optional.ofNullable(job).map(ETLJob::getScheduler).map(ETLScheduler::getSchedulerCron).orElse(null);
		if(StringUtils.isEmpty(cronExp)){
			return new ResponseData.Builder("").error("该任务还未设置执行计划！");
		}
		project.setCronExpression(cronExp);
		final List<String> moduleCodes = modules.stream().map(ETLModule::getModuleCode).collect(Collectors.toList());
		String filePath = createZipFile.createJobFileById(job, moduleCodes);
		String zipName = createZipFile.createZipByJobName(filePath, UUID.randomUUID().toString());
		if(StringUtils.isEmpty(zipName)){
			return new ResponseData.Builder("").error("任务上传失败，请检查该job下任务是否启用或者任务的依赖配置错误！");
		}else {
			project.setZipFileName(zipName);
			ResponseData<String> responseData = azkabanJobManageService.createOrUpdateProject(project);
			// 更新etlmodule published 状态值
			if(ResponseData.Status.SUCCESS.getCode().equals(responseData.getStatus())) {
				updatePublishStatus(jobId, moduleCodes);
			}
			createZipFile.deleteFile(zipFilePath + File.separator + zipName);
			return responseData;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void updatePublishStatus(int jobId, List<String> modules) {
		// 先将所有的任务切换成未发布，更新已发布成功的任务的状态
		moduleMapper.updatePublishedByJobId(false, jobId);
		moduleMapper.updatePublishedByCodes(true, modules);
	}


    public ResponseData.Page<ETLLogSummary> selectLogSummarys(int offset, int limit, Integer jobId, String moduleCode, Integer status, String hospital){

        PageHelper.offsetPage(offset, limit);
        Page<ETLLogSummary> pageData = (Page<ETLLogSummary>) logSummaryMapper.selectLogSummaryByJobId(jobId,moduleCode,status,hospital);

        return new ResponseData.Page<ETLLogSummary>(pageData.getTotal(),pageData);
    }

	public List<ETLLogDetail> selectLogDetailsBySummaryId(Integer summaryId) throws Exception{
		final ETLLogSummary etlLogSummary = logSummaryMapper.selectByPrimaryKey(Long.valueOf(summaryId));
		String filePath = DipConfig.getConfigInstance().getProperty("logback.dir","") + File.separator
			+ DateFormatUtils.format(etlLogSummary.getLogSummaryStart(), "yyyy-MM-dd")
			+ File.separator + etlLogSummary.getBatchId()+".log";
		File file = new File(filePath);
		ETLLogDetail total = new ETLLogDetail();
		total.setSubModuleName("总体");
		total.setLogDetailStart(etlLogSummary.getLogSummaryStart());
		total.setLogDetailEnd(etlLogSummary.getLogSummaryEnd());
		total.setStatus(etlLogSummary.getStatus());
		if(file.exists()) {
			FileReader fileReader = new FileReader(file);
			total.setLogContent(fileReader.readString());
		}
		final List<ETLLogDetail> detailList = moduleMapper.getLogDetailBySummaryId(summaryId);
		detailList.add(0,total);
		return detailList;
	}

    public void deleteJobById(Integer id) throws SQLException {
        EtlJobModule module=new EtlJobModule();
        module.setJobId(id);
        ETLJob job = jobMapper.selectByPrimaryKey(id);
        int i = jobModuleMapper.selectCount(module);
        if (i<=0){
            mapper.deleteByPrimaryKey(id);
            try {
                boolean flag =  azkabanJobManageService.isExistProject(new Project(job.getJobName(),"",""));
                if (flag) {
                    azkabanJobManageService.deleteProject(new Project(job.getJobName(),"",""));
                }
            } catch (Exception e) {
                logger.error("删除Azkaban 项目失败"+e.getMessage());
                e.printStackTrace();
            }
        }else {
            throw new SQLException("存在MODULE数据，不能删除");
        }

    }

    public List<ETLJob> getJobs(Integer topicId, String jobName) throws Exception{
       return moduleMapper.getJobs(topicId,jobName);
    }

    @Autowired
    TemplateMapper templateMapper;

    public List<Template> selectPublicTemplates() {
		return templateMapper.selectAllPublic();
	}

    public List<EtlJobVersion.JobHistory> getTemplateDescById(List<Template> templates , String id) {
		List<Template> resultList = new ArrayList<>();
		Optional<Template> template = templates.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst();
		if(template.isPresent()) {
			resultList.add(template.get());
			findTemplateById(resultList,templates, template.get().getId());
		}
		return resultList.stream()
			.map(s -> new EtlJobVersion.JobHistory(s.getCreatedAt(),s.getDesc())).collect(Collectors.toList());
	}

	void findTemplateById(List<Template> resultList, List<Template> list, String id) {
    	if(StringUtils.isEmpty(id)) {
    		return;
		}
    	if(list != null && !list.isEmpty()) {
    		Optional<Template> template = list.stream().filter(t -> id.equalsIgnoreCase(t.getPreTemplateId())).findFirst();
			template.ifPresent(resultList::add);
    		findTemplateById(resultList,list, template.map(Template::getId).orElse(""));
		}
	}

    public ETLJob checkJobName(String jobName) {
        return jobMapper.checkJobName(jobName);
    }

	public ETLJob checkJobName(Integer topicId, String jobName) {
		return jobMapper.checkJobNameUnderTopicId(jobName, topicId);
	}

    public List<ETLJob> listJobByName(String jobName) {
        if (StringUtils.isNotEmpty(jobName)){
            Example example = new Example(ETLJob.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andLike("jobName","%"+jobName+"%");
            List<ETLJob> jobs = jobMapper.selectByExample(example);
            return jobs;
        }
        return null;
    }

    public void updateLogsummaryEndDateBySummaryId(Date date, Long summaryId) {
        if (summaryId == 0) {
            return;
        }
        logSummaryMapper.updateLogsummaryEndDateBySummaryId(date, summaryId);

    }

    public StringBuffer realTimePrintLogs(String ip,String logPath) throws Exception{
        FtpHelper ftpHelper = new SftpHelper();
        StringBuffer stringBuffer = new StringBuffer();
        DipConfig.getConfigInstance().getDataxMachineInfos().stream()
                .filter(m -> StringUtils.equalsIgnoreCase(m.getHost(),ip)).findFirst().ifPresent(
                machineInfoPo -> {
                    ftpHelper.loginFtpServer(machineInfoPo.getHost(), machineInfoPo.getUsername(),
                            machineInfoPo.getPassword(), machineInfoPo.getPort(), 120, "PASV");
                    InputStream inputStream = null;
                    BufferedReader reader = null;
                    try{
                        inputStream = ftpHelper.getInputStream(logPath);
                        reader =  new BufferedReader(new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8), 8192);
                        reader.lines().forEach(l -> stringBuffer.append(l).append(IOUtils.LINE_SEPARATOR));

                    }catch (Exception e){
                        logger.error("ftp读取远程信息时出错", e);
                    }finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        }catch (Exception e) {
                            logger.error("关闭ftp读取远程信息时出错", e);
                        }
                        ftpHelper.logoutFtpServer();
                    }

                }
        );
        return stringBuffer;
    }

    public void updateLogsummaryAndLogdetails(Integer summaryId,Integer detailId) {
        if (summaryId > 0){
            logSummaryMapper.updateLogsummaryStatusBySummaryId(summaryId);
            logSummaryMapper.updateLogDetailByUuid(detailId);
        }
    }


    public List<ETLJobVo> selectJobTree(Integer topicId, String jobName, String moduleName){
		List<ETLJobVo> jobVoList = new ArrayList<>();
		List<ETLJob> etlJobList = moduleMapper.getJobs(topicId,jobName);
		final List<Template> templates = selectPublicTemplates();
		if(etlJobList != null && etlJobList.size() > 0){
			etlJobList.forEach(j->{
				ETLJobVo jobVo = new ETLJobVo();
				BeanUtil.copyProperties(j, jobVo);
				try {
					final Boolean existProject = azkabanJobManageService.isExistProject(new Project(j.getJobName(), "", ""));
					jobVo.setStatus(existProject?1:0);
					jobVo.setJobVersion(getTemplateDescById(templates, j.getTemplateId()));
				}catch (Exception e) {
					jobVo.setStatus(0);
				}
				jobVo.setModuleList(moduleMapper.selectModuleDetails(null, j.getId(), null,moduleName));
				jobVoList.add(jobVo);
			});
		}
		return jobVoList;
	}

	public void selectCheckStatusByJobId(Integer jobId, EtlJobVersion jobVersion) {
		final List<ModuleCheckStatus> list = jobMapper.selectModuleCheckStatusByJobId(jobId);
		List<String> checkStatus = new ArrayList<>();
		List<String> notCheckStatus = new ArrayList<>();
		Optional.ofNullable(list).ifPresent(l -> {
			l.forEach(s -> {
				if(s.getCount() == 0) {
					notCheckStatus.add(s.getModuleCode());
				}else {
					checkStatus.add(s.getModuleCode());
				}
			});
		});

		jobVersion.setCheckedModules(checkStatus);
		jobVersion.setNotCheckedModules(notCheckStatus);
	}
}
