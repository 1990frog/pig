package com.clinbrain.dip.strategy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.alibaba.fastjson.JSONObject;
import com.clinbrain.dip.connection.DatabaseMeta;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLScheduler;
import com.clinbrain.dip.rest.mapper.DBETLJobMapper;
import com.clinbrain.dip.rest.mapper.DBETLSchedulerMapper;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.rest.service.ConnectionService;
import com.clinbrain.dip.rest.service.JobService;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.PackageItem;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.bean.TemplateMatchVO;
import com.clinbrain.dip.strategy.config.CommonConfig;
import com.clinbrain.dip.strategy.constant.TacticsConstant;
import com.clinbrain.dip.strategy.entity.Template;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.strategy.sqlparse.FromTableItem;
import com.clinbrain.dip.strategy.util.CCJSqlParseUtil;
import com.clinbrain.dip.strategy.util.ZipFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_NAME_SUFFIX;
import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;

/**
 * (TTemplet)表服务实现类
 *
 * @author Liaopan
 * @since 2020-09-04 10:48:37
 */
@Service
@RequiredArgsConstructor
public class TemplateService extends BaseService<Template> {

	private final TemplateMapper templateMapper;

	private final CommonConfig commonConfig;

	private final ModuleService moduleService;

	private final ConnectionService connectionService;

	private final DBETLSchedulerMapper schedulerMapper;

	private final JobService jobService;

	private final DBETLJobMapper jobMapper;

	private static final String SYSTEM_TEMPLAT_PATH = "/beetl/system.json.tmpl";

	private ObjectMapper objectMapper = new ObjectMapper();

	public Template getByCode(String code) {
		return templateMapper.selectOneByExample(Weekend.of(Template.class).weekendCriteria().andEqualTo(Template::getCode, code));
	}

	/**
	 * 上传，保存模板文件
	 *
	 * @param file
	 * @throws Exception
	 */
	public void uploadSave(MultipartFile file) throws Exception {

		// 文件名直接用系统时间
		long fileName = System.currentTimeMillis();
		//保存文件
		File destFile = new File(commonConfig.getPackagePath() + File.separator + fileName + PACKAGE_NAME_SUFFIX);
		file.transferTo(destFile);

		ZipFile zipFile = new ZipFile(destFile);
		try {
			//验证文件
			Preconditions.checkArgument(zipFile.isValidZipFile(), "不是有效的模板文件");

			final PackageInfo packageInfo = ZipFileInfo.readZipSystemInfo(zipFile);
			final Template template = templateMapper.selectPublicTemplate(packageInfo.getId());

			Preconditions.checkArgument(template == null, "文件已经存在");

			saveTemplate(packageInfo, fileName + PACKAGE_NAME_SUFFIX, false);
		} catch (Exception e) {
			destFile.delete();
			throw e;
		}

	}

	public boolean removeTemplate(List<String> ids) {

		Weekend<Template> weekend = new Weekend<>(Template.class);
		final WeekendCriteria<Template, Object> criteria = weekend.weekendCriteria();
		criteria.andIn(Template::getId, ids).andEqualTo(Template::getCustom, true);
		final List<Template> templates = templateMapper.selectByExample(weekend);
		boolean result = true;
		for (Template t : templates) {
			if (FileUtil.del(commonConfig.getPackagePath() + t.getTmplPath())) {
				templateMapper.deleteByPrimaryKey(t);
			} else {
				result = false;
			}
		}
		return result;
	}


	/**
	 * 生成自定义模板文件, 根据jobId, 任务code
	 *
	 * @param packageInfo 模板属性
	 * @return
	 * @throws Exception
	 */
	public boolean generateTempletFile(PackageInfo packageInfo, Integer jobId, List<String> moduleCodes) throws Exception {

		// 根据jobId查找任务组名称和任务组关联的公用模板
		final ETLJob etlJob = jobService.getJobs(null, null).stream().filter(s -> jobId.equals(s.getId()))
			.findFirst().orElseThrow(NullPointerException::new);

		packageInfo.setPreTemplateId(etlJob.getTemplateId());
		packageInfo.setId(IdUtil.simpleUUID());
		packageInfo.setName(etlJob.getJobName());
		packageInfo.setCron(Optional.ofNullable(etlJob.getScheduler()).map(ETLScheduler::getSchedulerCron).orElse(""));
		packageInfo.setCronDesc(Optional.ofNullable(etlJob.getScheduler()).map(ETLScheduler::getSchedulerDesc).orElse(""));

		final TemplateConfig templateConfig = new TemplateConfig();
		templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
		TemplateEngine engine = TemplateUtil.createEngine(templateConfig);

		cn.hutool.extra.template.Template systemTemplate = engine.getTemplate(SYSTEM_TEMPLAT_PATH);

		//根据模板产生包描述文本
		final Map<String, Object> map = BeanUtil.beanToMap(packageInfo);
		map.put("jobId", packageInfo.getId());
		map.put("jobName", packageInfo.getName());
		final String systemInfo = systemTemplate.render(map);


		String zipPath = commonConfig.getPackagePath() + File.separator + System.currentTimeMillis() + PACKAGE_NAME_SUFFIX;

		File file = new File(zipPath);
		FileUtil.mkParentDirs(file);

		ZipFile zipFile = new ZipFile(zipPath);

		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());

		zipParameters.setFileNameInZip(TacticsConstant.PACKAGE_SYSTEM_INFO);

		InputStream is = new ByteArrayInputStream(systemInfo.getBytes(StandardCharsets.UTF_8));
		zipFile.addStream(is, zipParameters);
		try {
			for (String moduleCode : moduleCodes) {
				final ETLModule etlModule = moduleService.selectModuleDetailByCode(moduleCode);
				final ModuleTaskRequest moduleTaskRequest = moduleService.transformModule(etlModule);
				zipParameters.setFileNameInZip(moduleTaskRequest.getModuleCode() + "_m");
				String moduleInfo = objectMapper.writeValueAsString(moduleTaskRequest);
				is = new ByteArrayInputStream(moduleInfo.getBytes(StandardCharsets.UTF_8));
				zipFile.addStream(is, zipParameters);
				// 写入 workflow_sql
				JSONObject jsonObject = new JSONObject();
				etlModule.getWorkflows().forEach(workflow -> {
					jsonObject.put(workflow.getWorkflowCode(), workflow.getWorkflowSQL());
				});
				zipParameters.setFileNameInZip(moduleTaskRequest.getModuleCode() + "_s");
				is = new ByteArrayInputStream(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
				zipFile.addStream(is, zipParameters);
			}

			return saveTemplate(packageInfo, zipFile.getFile().getName(), true);
		} catch (Exception e) {
			logger.error("写入module信息出错", e);
			zipFile.getFile().delete();
			throw e;
		} finally {
			IoUtil.close(is);
		}
	}

	private boolean saveTemplate(PackageInfo packageInfo, String fileName, boolean custom) throws Exception {
		Template tTemplate = new Template();
		tTemplate.setId(packageInfo.getId());
		tTemplate.setCode(StringUtils.upperCase(packageInfo.getCode()));
		tTemplate.setTmplName(packageInfo.getName());
		tTemplate.setSystem(packageInfo.getSystem());
		tTemplate.setVendor(packageInfo.getVendor());
		tTemplate.setEdition(packageInfo.getEdition());
		tTemplate.setSubVersion(packageInfo.getSubVersion());
		tTemplate.setDesc(packageInfo.getDesc());
		tTemplate.setEnable(true);
		tTemplate.setCustom(custom);
		tTemplate.setTmplPath(fileName);
		tTemplate.setCreatedAt(new Date());
		tTemplate.setUpdatedAt(new Date());
		return insert(tTemplate) > 0;

	}

	/**
	 * 查询所有非自定义模板数据
	 *
	 * @return
	 */
	@Override
	public List<Template> selectAll() {
		Weekend<Template> weekend = new Weekend<>(Template.class);
		final WeekendCriteria<Template, Object> criteria = weekend.weekendCriteria();
		criteria.andNotEqualTo(Template::getCustom, 1).orIsNull(Template::getCustom);
		return selectByExample(weekend);
	}

	/**
	 * 匹配模板
	 *
	 * @throws Exception
	 */
	public List<TemplateMatchVO> matching(List<SystemConnectionCodeVO> list) {
		List<TemplateMatchVO> resultList = new ArrayList<>();
		if (list != null && !list.isEmpty()) {
			list.forEach(template -> {
				final List<String> ids = template.getTemplateIdList();
				Weekend<Template> weekend = new Weekend<>(Template.class);
				final WeekendCriteria<Template, Object> criteria = weekend.weekendCriteria();
				criteria.andIn(Template::getId, ids).andNotEqualTo(Template::getCustom, 1);
				final List<Template> templates = selectByExample(weekend);
				if (templates != null && !templates.isEmpty()) {
					templates.forEach(t -> {
						TemplateMatchVO matchVO = new TemplateMatchVO();
						resultList.add(matchVO);
						matchVO.setTemplateInfo(t);
						try {
							// 拿到包里面的所有任务，计算匹配度
							final List<PackageItem> packageItems
								= ZipFileInfo.readZipFiles(commonConfig.getPackagePath() + File.separator + t.getTmplPath());
							final Double rate = templateRate(packageItems, template.getConnectionCode());
							matchVO.setMatchedRate(rate);
						} catch (Exception e) {
							logger.error("读取策略包出错", e);
						}
					});
				}

			});
		}

		return resultList;
	}

	private Double templateRate(List<PackageItem> packageItems, String connectionCode) {

		try {

			// 查找包中 datax 组件的sql语句
			final List<String> dataXComponentSqls = packageItems.stream().map(PackageItem::getWorkflowSqlMap)
				.map(m -> m.keySet().stream()
					.filter(s -> s.contains("DataXComponent")).map(m::get).collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

			Set<FromTableItem> tableColumns = new HashSet<>();
			for (String sql : dataXComponentSqls) {
				try {
					tableColumns.addAll(CCJSqlParseUtil.getTableColumns(sql));
				} catch (Exception e) {
					logger.error("解析sql:{} 出错", sql, e);
				}
			}
			if(tableColumns.isEmpty()) {
				return 0d;
			}

			Set<String> dbs = new HashSet<>();
			Set<String> tables = new HashSet<>();
			// 列总数
			final long totalColumns = tableColumns.parallelStream().map(FromTableItem::getColumnItems).mapToLong(List::size).sum();


			tableColumns.forEach(item -> {
				dbs.add(StringUtils.defaultIfEmpty(item.getDatabaseName(), item.getTableSchema()).toLowerCase());
				tables.add(StringUtils.lowerCase(item.getTableName()));
			});

			final List<DatabaseMeta> dbTemps = connectionService.getDataBases(connectionCode, "", "");
			// 获取所有database
			final Set<String> localDbs = dbTemps.stream().map(DatabaseMeta::getName).collect(Collectors.toSet());

			final List<String> subDBs = CollUtil.subtractToList(dbs, localDbs);// 多出来的db
			dbs.removeAll(subDBs);
			logger.info("有这几个数据库在本地没有找到 {}", CollUtil.join(subDBs, ","));
			// 查找所有这个db下的内容, 去掉db 不存在的记录
			tableColumns.removeIf(t -> subDBs.contains(StringUtils.lowerCase(t.getDatabaseName())));
			logger.info("删除元素后继续查找 {}", tableColumns.size());

			// 循环判断db中的表：，去掉db中的表不存在的记录
			for (String dbName : dbs) {
				final List<DatabaseMeta> tableTemps = connectionService.getDataBases(connectionCode, dbName, "");
				if (tableTemps != null && !tableTemps.isEmpty()) {
					final Set<String> tempTableSet = tableTemps.stream().map(DatabaseMeta::getTableMetas).flatMap(Collection::parallelStream)
						.map(tableMeta -> tableMeta.tableName).collect(Collectors.toSet());
					tableColumns.removeIf(t -> {
							String tempName = StringUtils.isEmpty(t.getTableSchema()) ? t.getTableName() : t.getTableSchema() + "." + t.getTableName();
							return dbName.equalsIgnoreCase(t.getDatabaseName()) &&
								!CollUtil.contains(tempTableSet,(Predicate<String>) str -> str.equalsIgnoreCase(tempName));
						}
					);

					// 根据表来删除不存在的列
					tableColumns.forEach(cc -> {
						final List<DatabaseMeta> tableColumnTemps =
							connectionService.getDataBases(connectionCode, cc.getDatabaseName(), cc.getTableName());
						final Set<String> columnTemps = tableColumnTemps.stream().map(DatabaseMeta::getTableMetas).flatMap(Collection::parallelStream)
							.map(tableMeta -> tableMeta.allColumns).flatMap(Collection::parallelStream)
							.map(t -> t.name).collect(Collectors.toSet());

						//单个表的列与本地比对
						cc.getColumnItems().removeIf(c -> !CollUtil.contains(columnTemps, (Predicate<String>)s -> s.equalsIgnoreCase(c.getColumnName())));
					});
				}
			}

			final long validColumns = tableColumns.parallelStream().map(FromTableItem::getColumnItems).mapToLong(List::size).sum();
			return NumberUtil.div(validColumns, totalColumns);

		} catch (Exception e) {
			logger.error("计算模板匹配度出错", e);
			return 0d;
		}
		//totalColumns/validColumns
	}

	/**
	 * 根据cron 表达式查询
	 * @param cron
	 * @return
	 */
	private ETLScheduler getSchedulerByCron(String cron) {
		Weekend<ETLScheduler> weekend = new Weekend<>(ETLScheduler.class);
		final WeekendCriteria<ETLScheduler, Object> criteria = weekend.weekendCriteria();
		criteria.andEqualTo(ETLScheduler::getSchedulerCron, cron);
		final List<ETLScheduler> schedulers = schedulerMapper.selectByExample(weekend);
		if(schedulers.size() > 0) {
			return schedulers.get(0);
		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean importSaveModule(Integer topicId, String hospitalCode, String templateId) throws Exception {
		final Template template = templateMapper.selectPublicTemplate(templateId);
		String zipFilePath = commonConfig.getPackagePath() + File.separator + template.getTmplPath();
		final List<PackageItem> packageItems
			= ZipFileInfo.readZipFiles(zipFilePath);
		if(packageItems == null || packageItems.isEmpty()) {
			throw new RuntimeException("没有找到模板对应的任务信息，请确认模板文件正确！");
		}
		final PackageInfo packageInfo = ZipFileInfo.readZipSystemInfo(zipFilePath);
		if(packageInfo == null) {
			throw new RuntimeException("模板文件读取失败，请确认文件正确！");
		}

		ETLScheduler scheduler = getSchedulerByCron(packageInfo.getCron());
		if(scheduler == null) {
			scheduler = new ETLScheduler();
			scheduler.setSchedulerDesc(packageInfo.getCronDesc());
			scheduler.setSchedulerCron(packageInfo.getCron());
			scheduler.setCreatedAt(new Date());
			scheduler.setUpdatedAt(new Date());
			schedulerMapper.insertSelective(scheduler);
		}

		// 保存job信息和任务关联信息,查找已经存在的job供使用

		ETLJob etlJob = new ETLJob();
		etlJob.setJobName(template.getTmplName());
		etlJob.setTopicId(""+topicId);
		etlJob.setSchedulerId(scheduler.getSchedulerId());
		etlJob.setEnabled(0);
		etlJob.setTemplateId(template.getId());
		etlJob.setCreatedAt(new Date());
		etlJob.setUpdatedAt(new Date());
		final ETLJob job = jobService.checkJobName(topicId, etlJob.getJobName());
		int jobId = 0;
		if(job == null) {
			jobService.insert(etlJob);
			jobId = etlJob.getId();
		}else {
			jobId = job.getId();
		}

		boolean result = true;
		for (PackageItem item : packageItems) {
			final ModuleTaskRequest moduleInfo = item.getModuleInfo();
			moduleInfo.setModuleCode(null);
			moduleInfo.setHospitalName(hospitalCode);
			moduleInfo.setCreatedAt(new Date());
			moduleInfo.setJobId(jobId);
			result = moduleService.editEtlModule(moduleInfo);
		}
		return result;
	}

	public Template templateFilePath(String id) {
		final Template template = templateMapper.selectCustomTemplate(id);
		if (template != null) {
			String filePath = commonConfig.getPackagePath() + File.separator + template.getTmplPath();
			if (FileUtil.exist(filePath)) {
				template.setTmplPath(filePath);
			} else {
				template.setTmplPath(null);
			}
		}
		return template;
	}
}
