package com.clinbrain.dip.services;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.rest.service.ModuleService;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.mapper.TemplateMapper;
import com.clinbrain.dip.strategy.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;

/**
 * Created by Liaopan on 2020-09-07.
 */
@SpringBootTest
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

		final PackageInfo packageInfo = new PackageInfo("费用相关", "卫宁", "HIS", "4.5", "1", "公版第一版");
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
}
