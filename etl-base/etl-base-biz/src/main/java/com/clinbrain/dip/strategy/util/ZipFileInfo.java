package com.clinbrain.dip.strategy.util;

import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.PackageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import parquet.Preconditions;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;
import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_SYSTEM_INFO;

/**
 * Created by Liaopan on 2020-09-05.
 */
public class ZipFileInfo {

	private static ObjectMapper objectMapper = new ObjectMapper();

	public static PackageInfo readZipSystemInfo(ZipFile zipFile) throws Exception {

		InputStream is = null;
		try {
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());
			}

			final FileHeader systemFile = zipFile.getFileHeader(PACKAGE_SYSTEM_INFO);
			Preconditions.checkNotNull(systemFile, "不是有效的策略包文件! ");
			is = zipFile.getInputStream(systemFile);
			final byte[] bytes = IOUtils.toByteArray(is);
			return objectMapper.readValue(new String(bytes), PackageInfo.class);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// 获取文件中的内容
	public static List<PackageItem> readZipFiles(String path) throws Exception {

		System.out.println(path);
		ZipFile zipFile = new ZipFile(path);
		zipFile.setCharset(Charset.forName("GBK"));
		InputStream is = null;
		InputStream mStream = null;
		List<PackageItem> packageItems = new ArrayList<>();
		try {
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());
			}

			final FileHeader systemFile = zipFile.getFileHeader(PACKAGE_SYSTEM_INFO);
			Preconditions.checkNotNull(systemFile, "不是有效的策略包文件! ");

			List<FileHeader> fileHeaders = zipFile.getFileHeaders();
			// 过滤掉
			if(fileHeaders != null && !fileHeaders.isEmpty()) {
				fileHeaders = fileHeaders.stream().filter(f -> StringUtils.endsWith(f.getFileName(),"_s"))
					.collect(Collectors.toList());

				for(FileHeader pFile : fileHeaders) {
					PackageItem packageItem = new PackageItem();
					is = zipFile.getInputStream(pFile);
					mStream = zipFile.getInputStream(zipFile.getFileHeader(
						StringUtils.substringBeforeLast(pFile.getFileName(),"_s")+"_m"));
					packageItem.setModuleCode(StringUtils.substringBeforeLast(pFile.getFileName(),"_s"));
					packageItem.setModuleInfo(objectMapper.readValue(new String(IOUtils.toByteArray(mStream)), ModuleTaskRequest.class));
					packageItem.setWorkflowSqlMap(objectMapper.readValue(new String(IOUtils.toByteArray(is)), Map.class));
					packageItems.add(packageItem);
				}

			}

			return packageItems;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
