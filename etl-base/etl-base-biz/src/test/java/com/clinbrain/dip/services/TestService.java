package com.clinbrain.dip.services;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.clinbrain.dip.ETLWebApplication;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLWorkflow;
import com.clinbrain.dip.pojo.ETLWorkflowSelectRegex;
import com.clinbrain.dip.pojo.EtlJobModule;
import com.clinbrain.dip.rest.bean.EtlJobVersion;
import com.clinbrain.dip.rest.mapper.DBETLJobModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLLogSummaryMapper;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.service.JobService;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.rest.util.CreateZipFile;
import com.clinbrain.dip.rest.vo.ModuleWorkflowStatus;
import com.clinbrain.dip.strategy.bean.ModuleDependencyVO;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.strategy.service.EtlWorkflowSelectRegexService;
import com.clinbrain.dip.strategy.service.SqlQueryService;
import com.clinbrain.dip.strategy.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.pig4cloud.pig.common.core.util.R;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;
import tk.mybatis.mapper.weekend.WeekendSqls;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;

/**
 * Created by Liaopan on 2020-09-07.
 */
@SpringBootTest(classes = ETLWebApplication.class)
@RunWith(SpringRunner.class)
public class TestService {

	@Autowired
	private TemplateMapper mapper;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private TemplateService templateService;

	@Test
	@Rollback
	public void test() {
		final ModuleTaskRequest etlModule = moduleService.selectModuleDetail("ETL_DIM_public.pa_op_registration_t-001_1591926533179");

		final PackageInfo packageInfo = new PackageInfo("","费用相关", "卫宁", "HIS", "4.5", 1, "公版第一版","","0 1 0 0 0 ?", "每天零时");
		final TemplateConfig templateConfig = new TemplateConfig();
		templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
		TemplateEngine engine = TemplateUtil.createEngine(templateConfig);

		cn.hutool.extra.template.Template template = engine.getTemplate("/beetl/system.json.tmpl");
		Template moduleTemplate = engine.getTemplate("/beetl/module.json.tmpl");

		final String render = template.render(BeanUtil.beanToMap(packageInfo));

		String zippath = "E:/data/abc.clb";
		/** 一次性压缩多个文件，文件存放至一个文件夹中*/
		try {
			ZipFile zipFile = new ZipFile(zippath);

			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(EncryptionMethod.AES);
			zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
			zipParameters.setFileNameInZip("system.json");

			zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());

			zipFile.addStream(new ByteArrayInputStream(render.getBytes(StandardCharsets.UTF_8)), zipParameters);

			zipParameters.setFileNameInZip(etlModule.getModuleName() +".json");
			ObjectMapper objectMapper = new ObjectMapper();
			final Map map = objectMapper.readValue(objectMapper.writeValueAsString(etlModule), Map.class);
			zipFile.addStream(new ByteArrayInputStream(moduleTemplate.render(map).getBytes()), zipParameters);

			System.out.println(zipFile.getFile().length());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(R.ok(templateService.selectPageAll(0,5,null)));
		System.out.println(objectMapper.writeValueAsString(templateService.selectPageAll(0,5,null)));
		System.out.println(templateService.selectPageAll(0,5,null));
	}

	@Autowired
	private DBETLLogSummaryMapper logSummaryMapper;

	@Test
	public void test233() throws Exception {
		final ArrayList<String> codes = Lists.newArrayList("123", "11", "12322a", "WN_HIS4.5_FYXG_1.0");
	}

	@Test
	public void testMatchTemplate() {
		List<SystemConnectionCodeVO> list = new ArrayList<>();
		SystemConnectionCodeVO vo = new SystemConnectionCodeVO();
		vo.setConnectionCode("oracledemo");
		vo.setSystem("HIS");
		vo.setTemplateIdList(Lists.newArrayList("829d01de60434788832c2a1a896c4054"));
		list.add(vo);
		System.out.println(templateService.matching(list));
	}

	@Autowired
	private JobService jobService;

	@Test
	public void testJob() {
		final List<com.clinbrain.dip.strategy.entity.Template> templates = jobService.selectPublicTemplates();
		final List<EtlJobVersion.JobHistory> descById = jobService.getTemplateDescById(templates, "25b81f8221db4980baf244418581087f");
		System.out.println(descById);
	}

	@Test
	public void testAzkabanUpload() throws Exception{
		ETLJob job = new ETLJob();
		job.setId(1);

		jobService.upload(97);
	}
	@Autowired
	private CreateZipFile zipFile;

	@Test
	public void testCreateJob() throws Exception{
		//zipFile.createJobFileByJobId(1);

		final List<ModuleDependencyVO> moduleList = moduleService.dependencyList(97);
		System.out.println(JSONUtil.toJsonStr(moduleList));

	}

	@Autowired
	private DBETLModuleMapper moduleMapper;

	@Test
	public void testDeleteModule() {
		/*String moduleCode = "ETL_FACT_chart_general_t-001_1605611546857";
		final ETLModule etlModule = moduleService.selectOne(moduleCode);
		moduleMapper.updateDependencyByModuleCode(moduleCode, etlModule.getDependencyCode());*/

		ETLJob etlJob = new ETLJob();
		etlJob.setJobName("测试插入");
		etlJob.setTopicId(""+27);
		etlJob.setSchedulerId(8);
		etlJob.setEnabled(0);
		etlJob.setTemplateId("a1d54f9bb6384204ab98f983c9cf578b");
		etlJob.setCreatedAt(new Date());
		etlJob.setUpdatedAt(new Date());
		jobService.insert(etlJob);
	}

	@Autowired
	private DBETLJobModuleMapper jobModuleMapper;

	@Test
	public void testDeleteModules() throws Exception{
		final List<EtlJobModule> etlJobModules = jobModuleMapper.selectByExample(new Example.Builder(EtlJobModule.class)
			.where(WeekendSqls.<EtlJobModule>custom().andEqualTo(EtlJobModule::getJobId, 153)).build());

		System.out.println(etlJobModules.size());
		for (EtlJobModule etlJobModule : etlJobModules) {
			System.out.println(etlJobModule.getModuleCode());
			moduleService.deleteModuleByModuleCode(etlJobModule.getModuleCode());
		}
	}

	@Test
	public void test3() {
		final ModuleWorkflowStatus moduleWorkflowStatuses
			= moduleMapper.selectWorkflowStatus("ETL_OTHER_public.ab_op_feelist_import_0000_1606183316387", "fc5ce2ae-6278-4c4e-9e11-4f5455024aa7");
		System.out.println(moduleWorkflowStatuses);
	}

	@Autowired
	private EtlWorkflowSelectRegexService regexService;

	@Test
	public void test4() {
		//final ETLModule etlModule = moduleService.selectModuleDetailByCode("ETL_FACT_regex_test_SZSL-001_1610097246982");
		List<ETLWorkflowSelectRegex> regexes = new ArrayList<>();
		final ETLWorkflowSelectRegex etlWorkflowSelectRegex = new ETLWorkflowSelectRegex();
		etlWorkflowSelectRegex.setWorkflowCode("");
		etlWorkflowSelectRegex.setColumnName("");
		etlWorkflowSelectRegex.setColumnType("");
		etlWorkflowSelectRegex.setCommonExpression("");
		etlWorkflowSelectRegex.setJoinMark("");
		etlWorkflowSelectRegex.setSort(0);

		final ETLWorkflowSelectRegex etlWorkflowSelectRegex2 = new ETLWorkflowSelectRegex();
		etlWorkflowSelectRegex2.setWorkflowCode("123");
		etlWorkflowSelectRegex2.setColumnName("112");
		etlWorkflowSelectRegex2.setColumnType("123");
		etlWorkflowSelectRegex2.setCommonExpression("123");
		etlWorkflowSelectRegex2.setJoinMark("123");
		etlWorkflowSelectRegex2.setSort(1);


		regexes.add(etlWorkflowSelectRegex);
		regexes.add(etlWorkflowSelectRegex2);
		regexService.saveAll(regexes);
	}

	@Test
	public void testDelete() throws Exception {
		ETLWorkflow workflow = new ETLWorkflow();
		ETLWorkflow workflow2 = new ETLWorkflow();
		workflow.setWorkflowCode("123");
		workflow2.setWorkflowCode("123");
		regexService.deleteByWorkflowCode(Lists.newArrayList(workflow2,workflow));
	}
	@Autowired
	private SqlQueryService sqlQueryService;

	@Test
	public void test5() throws Exception{
		final List<Map<String, Object>> sqlserver114 = sqlQueryService.queryList("sqlserver114", "select * from [CDA_Main]");
		System.out.println(sqlserver114.size());
	}

}
