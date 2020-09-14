package com.clinbrain.dip.strategy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.alibaba.fastjson.JSONObject;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.rest.service.JobService;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.config.CommonConfig;
import com.clinbrain.dip.strategy.entity.Template;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.strategy.util.ZipFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import parquet.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

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

	private final TemplateMapper mapper;

	private final CommonConfig commonConfig;

	private final ModuleService moduleService;

	private final JobService jobService;

	private static final String SYSTEM_TEMPLAT_PATH = "/beetl/system.json.tmpl";

	private static final String MODULE_TEMPLAT_PATH = "/beetl/module.json.tmpl";

	private ObjectMapper objectMapper = new ObjectMapper();

	public Template getByCode(String code) {
		return mapper.getByCode(code);
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

			saveOrUpdateTemplate(packageInfo, fileName + PACKAGE_NAME_SUFFIX, false);
		} catch (Exception e) {
			destFile.delete();
			throw e;
		}

	}

	/**
	 * 生成自定义模板文件, 根据jobId, 任务code
	 *
	 * @param packageInfo 模板属性
	 * @return
	 * @throws Exception
	 */
	public boolean generateTempletFile(PackageInfo packageInfo, int jobId, List<String> moduleCodes) throws Exception {

		final ETLJob etlJob = jobService.selectOne(jobId);

		final TemplateConfig templateConfig = new TemplateConfig();
		templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
		TemplateEngine engine = TemplateUtil.createEngine(templateConfig);

		cn.hutool.extra.template.Template systemTemplate = engine.getTemplate(SYSTEM_TEMPLAT_PATH);

		final String systemInfo = systemTemplate.render(BeanUtil.beanToMap(packageInfo));


		String zipPath = commonConfig.getPackagePath() + File.separator + etlJob.getJobName() + PACKAGE_NAME_SUFFIX;
		File file = new File(zipPath);
		if(file.exists()) {
			file.delete();
		}

		ZipFile zipFile = new ZipFile(zipPath);

		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());

		zipParameters.setFileNameInZip("system");

		InputStream is = new ByteArrayInputStream(systemInfo.getBytes(StandardCharsets.UTF_8));
		zipFile.addStream(is, zipParameters);
		try {
			for (String moduleCode : moduleCodes) {
				final ETLModule etlModule = moduleService.selectModuleDetailByCode(moduleCode);
				final ModuleTaskRequest moduleTaskRequest = moduleService.transformModule(etlModule);
				zipParameters.setFileNameInZip(moduleTaskRequest.getModuleName() + ".json");
				String moduleInfo = objectMapper.writeValueAsString(moduleTaskRequest);
				is = new ByteArrayInputStream(moduleInfo.getBytes(StandardCharsets.UTF_8));
				zipFile.addStream(is, zipParameters);
				// 写入 workflow_sql
				JSONObject jsonObject = new JSONObject();
				etlModule.getWorkflows().forEach(workflow -> {
					jsonObject.put(workflow.getWorkflowCode(),workflow.getWorkflowSQL());
				});
				zipParameters.setFileNameInZip(moduleTaskRequest.getModuleName() + "_s.json");
				is = new ByteArrayInputStream(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
				zipFile.addStream(is, zipParameters);
			}

			return saveOrUpdateTemplate(packageInfo, zipFile.getFile().getName(), true);
		}catch (Exception e) {
			logger.error("写入module信息出错", e);
			zipFile.getFile().delete();
			throw e;
		} finally {
			IoUtil.close(is);
		}
	}

	private boolean saveOrUpdateTemplate(PackageInfo packageInfo, String fileName, boolean custom) throws Exception{
		String code = StrUtil.format("{}_{}{}_{}_{}", PinyinUtil.getFirstLetter(packageInfo.getVendor(), ""),
			packageInfo.getSystem(), packageInfo.getEdition(), PinyinUtil.getFirstLetter(packageInfo.getName(), ""),
			packageInfo.getSubVersion(), fileName);

		Template tTemplate = new Template();

		tTemplate.setCode(StringUtils.upperCase(code));
		tTemplate.setTmplName(packageInfo.getName());
		tTemplate.setSystem(packageInfo.getSystem());
		tTemplate.setVendor(packageInfo.getVendor());
		tTemplate.setEdition(packageInfo.getEdition());
		tTemplate.setSubVersion(packageInfo.getSubVersion());
		tTemplate.setDescription(packageInfo.getDescription());
		tTemplate.setEnable(true);
		tTemplate.setCustom(custom);
		tTemplate.setTmplPath(fileName);
		final Template one = selectOne(tTemplate.getCode());
		if(one == null) {
			tTemplate.setCreatedAt(new Date());
			tTemplate.setUpdatedAt(new Date());
			return insert(tTemplate) > 0 ;
		}else  {
			tTemplate.setUpdatedAt(new Date());
			return updateNonNull(tTemplate) > 0;
		}
	}
}
