package com.clinbrain.dip;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.json.JSONUtil;
import com.clinbrain.dip.sqlparse.ParseSql;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.util.SqlParseUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;

/**
 * Created by Liaopan on 2020/7/30 0030.
 */

public class Test {

	@org.junit.Test
	public void test1() throws Exception {
		BufferedReader fileReader
			= new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\User\\Desktop\\sql脚本.sql"), Charset.forName("GB2312")));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = fileReader.readLine()) != null) {
			sb.append(line);
		}
		System.out.println(sb);
		//SqlParseUtil.parseSql(sb.toString(), ParseSql.SqlType.MYSQL);
	}

	@org.junit.Test
	public void testZipFile() {

		final PackageInfo packageInfo = new PackageInfo("费用相关", "v1.0", "HIS");
		final TemplateConfig templateConfig = new TemplateConfig();
		templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
		TemplateEngine engine = TemplateUtil.createEngine(templateConfig);

		Template template = engine.getTemplate("/beetl/system.json.tmpl");
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

			for(int i =0;i < 10; i++) {
				zipParameters.setFileNameInZip("i_" +i +".json");
				zipFile.addStream(new ByteArrayInputStream(("第"+i+"个文件").getBytes()), zipParameters);
			}

			System.out.println(zipFile.getFile().length());
			// 添加签名
			final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);
			final FileInputStream fileInputStream = new FileInputStream(zipFile.getFile());
			byte[] cc = new byte[fileInputStream.available()];
			fileInputStream.read(cc);
			byte[] signed = sign.sign(cc);
			System.out.println("signed(长度):" + signed.length);
			ZipParameters ccParams = new ZipParameters();
			ccParams.setFileNameInZip("cc");
			zipFile.addStream(new ByteArrayInputStream(signed), ccParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@org.junit.Test
	public void testUnzipFile() throws IOException {
		ZipFile zipFile = new ZipFile("E:/data/abc.clb");

		if(zipFile.isEncrypted()) {
			zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());
		}
		System.out.println("验证zip文件： " + zipFile.isValidZipFile());
		final FileHeader ccFile = zipFile.getFileHeader("cc");
		InputStream is = zipFile.getInputStream(ccFile);
		final byte[] signed = IOUtils.toByteArray(is);
		System.out.println("is大小：" + is.available());
		System.out.println("signed大小：" + signed.length);

		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

		ZipFile newFile = new ZipFile(zipFile.getFile().getName(), PACKAGE_PASSWORD.toCharArray());
		zipFile.getFileHeaders().stream()
			.filter(f -> !"cc".equalsIgnoreCase(f.getFileName())).forEach(f -> {
				try {
					zipParameters.setFileNameInZip(f.getFileName());
					newFile.addStream(zipFile.getInputStream(f), zipParameters);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		// 获取验证
		final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);
		final FileInputStream fileInputStream = new FileInputStream(newFile.getFile());
		byte[] fileContent = new byte[fileInputStream.available()];
		fileInputStream.read(fileContent);

		final boolean verify = sign.verify(fileContent, signed);

		System.out.println("验证通过？ " + verify);
	}


	@org.junit.Test
	public void testUnzip() throws IOException {
		PackageInfo packageInfo = new PackageInfo("费用相关","威宁","HIS","4.5","1.0","测试");
		Console.log(JSONUtil.toJsonStr(packageInfo));
	}

	@org.junit.Test
	public void testSign() throws IOException {
		final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);

		FileInputStream reader = new FileInputStream(new File("E:\\data\\aa.clb"));
		final int size = reader.available();
		System.out.println("总大小：" + size);
		byte[] content = new byte[size];
		reader.read(content);
		System.out.println("读取大小：" + content.length);
		byte[] signed = sign.sign(content);

		System.out.println(sign.verify(content, signed));
		System.out.println(sign.getPrivateKey().toString());
		System.out.println(sign.getPublicKey().toString());
	}

}
