package com.clinbrain.dip.rest.util;

import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.mapper.DBETLJobMapper;
import com.clinbrain.dip.rest.mapper.DBETLLogSummaryMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class CreateZipFile {

	private static Logger logger = LoggerFactory.getLogger(CreateZipFile.class);

	private String zipFilePath = System.getProperty("java.io.tmpdir");

	@Value("${azkabanJobManage.commond}")
	private String commond;

	@Value("${azkabanJobManage.fistCommond}")
	private String fistCommond;

	@Value("${azkabanJobManage.endCommond}")
	private String endCommond;

	@Autowired
	private DBETLLogSummaryMapper JobModuleMapper;

	@Autowired
	private DBETLJobMapper jobMapper;

	/**
	 * 给指定jobId号创建File文件,并返回job文件路径
	 *
	 * @param jobId
	 * @throws Exception
	 */
	public String createJobFileByJobId(int jobId) throws Exception {
		List<Map> jobModules = new ArrayList<>();
		FileOutputStream fos = null;
		ETLJob job = jobMapper.selectByPrimaryKey(jobId);
		jobModules = JobModuleMapper.selectJobModuleByJobId(jobId);
		//创建job名称的临时文件夹

		String jobFilePath = zipFilePath + File.separator + job.getJobName() + "_" + System.currentTimeMillis();

		try {
			File jobFile = new File(jobFilePath);
			if (!jobFile.exists()) {
				jobFile.mkdirs();
			}
			//为每个module创建job文件
			String dependencies = null;
			for (int i = 0; i < jobModules.size(); i++) {

				String moduleIds = (String) Optional.ofNullable(jobModules.get(i).get("module_ids")).orElse("");
				String moduleNames = (String) Optional.ofNullable(jobModules.get(i).get("module_names")).orElse("");

				String[] ids = moduleIds.split(",");
				String[] names = moduleNames.split(",");
				for (int j = 0; j < ids.length; j++) {
					String id = ids[j];
					String name = names[j];
					File moduleFile = new File(jobFilePath + File.separator + name + ".job");
					fos = new FileOutputStream(moduleFile, false);
					if (i == 0) {
						fos.write(String.format(fistCommond, name, id).getBytes());
					} else {
						fos.write(String.format(commond, name, dependencies, id).getBytes());
					}
					fos.flush();
					fos.close();
				}
				dependencies = moduleNames;
			}
			//job文件生成后，添加一个以end.job文件
			File moduleFile = new File(jobFilePath + File.separator + "end.job");
			fos = new FileOutputStream(moduleFile, false);
			fos.write(String.format(endCommond, dependencies).getBytes());
			fos.flush();
			fos.close();
		} finally {
			IOUtils.closeQuietly(fos);
		}
		return jobFilePath;
	}

	/**
	 * 使用依赖的方式配置优先级, 创建azkaban .job文件
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	public String createJobFileById(int jobId) throws Exception {

		ETLJob job = jobMapper.selectByPrimaryKey(jobId);
		List<ETLModule> jobModules = jobMapper.selectModulesByJobId(jobId);
		//创建job名称的临时文件夹

		String jobFilePath = zipFilePath + File.separator + job.getJobName() + "_" + System.currentTimeMillis();


		File jobFile = new File(jobFilePath);
		if (!jobFile.exists()) {
			jobFile.mkdirs();
		}
		//为每个module创建job文件
		//1. 先找到所有的头部任务
		final List<ETLModule> firstModules = jobModules.stream()
			.filter(m -> StringUtils.isEmpty(m.getDependencyCode())).collect(Collectors.toList());
		List<String> end= Lists.newArrayList();
		for (ETLModule first : firstModules) {
			String moduleName = Optional.ofNullable(first.getModuleName()).orElse(first.getModuleCode());
			File moduleFile = new File(jobFilePath + File.separator + moduleName + ".job");
			try (FileOutputStream fos = new FileOutputStream(moduleFile, false)) {
				fos.write(String.format(fistCommond, moduleName, first.getModuleCode()).getBytes());
				fos.flush();
			}
			findModules(jobModules, first.getModuleCode(), end, moduleName, jobFilePath);
		}

		//job文件生成后，添加一个以end.job文件
		File moduleFile = new File(jobFilePath + File.separator + "end.job");
		try(FileOutputStream fos = new FileOutputStream(moduleFile, false)) {
			fos.write(String.format(endCommond, StringUtils.join(end,",")).getBytes());
			fos.flush();
		}

		return jobFilePath;
	}

	public String createJobFileById(ETLJob job, List<String> moduleCodes) throws Exception {

		List<ETLModule> jobModules = jobMapper.selectModulesByJobIdAndCode(job.getId(),moduleCodes);
		//创建job名称的临时文件夹
		String jobFilePath = zipFilePath + File.separator + job.getJobName() + "_" + System.currentTimeMillis();

		File jobFile = new File(jobFilePath);
		if (!jobFile.exists()) {
			jobFile.mkdirs();
		}
		//为每个module创建job文件
		//1. 先找到所有的头部任务
		final List<ETLModule> firstModules = jobModules.stream()
				.filter(m -> StringUtils.isEmpty(m.getDependencyCode())).collect(Collectors.toList());
		List<String> end= Lists.newArrayList();
		for (ETLModule first : firstModules) {
			String moduleName = Optional.ofNullable(first.getModuleName()).orElse(first.getModuleCode());
			File moduleFile = new File(jobFilePath + File.separator + moduleName + ".job");
			try (FileOutputStream fos = new FileOutputStream(moduleFile, false)) {
				fos.write(String.format(fistCommond, moduleName, first.getModuleCode()).getBytes());
				fos.flush();
			}
			findModules(jobModules, first.getModuleCode(), end, moduleName, jobFilePath);
		}

		//job文件生成后，添加一个以end.job文件
		File moduleFile = new File(jobFilePath + File.separator + "end.job");
		try(FileOutputStream fos = new FileOutputStream(moduleFile, false)) {
			fos.write(String.format(endCommond, StringUtils.join(end,",")).getBytes());
			fos.flush();
		}

		return jobFilePath;
	}

	void findModules(List<ETLModule> dataList, String moduleCode, List<String> endList, String dependencyName, String jobFilePath) {
		final List<ETLModule> collect = dataList.stream()
			.filter(m -> StringUtils.equalsIgnoreCase(m.getDependencyCode(), moduleCode)).collect(Collectors.toList());

		if (collect.isEmpty()) {
			endList.add(dependencyName);
		}

		collect.forEach(module -> {
			String moduleName = Optional.ofNullable(module.getModuleName()).orElse(module.getModuleCode());
			File moduleFile = new File(jobFilePath + File.separator + module.getModuleName() + ".job");
			try (FileOutputStream fos = new FileOutputStream(moduleFile, false)) {
				fos.write(String.format(commond, moduleName, dependencyName, module.getModuleCode()).getBytes());
				fos.flush();
				findModules(dataList, module.getModuleCode(), endList, moduleName, jobFilePath);
			} catch (IOException e) {
				logger.error("写入azkaban .job文件出错", e);
			}
		});
	}

	/**
	 * 将给定的文件夹中所有文件打成zip包到指定zip路径中
	 *
	 * @param filesPath
	 * @param zipPath
	 * @return
	 * @throws Exception
	 */
	public void createZipByFilePath(String filesPath, String zipPath) throws Exception {
		File file = new File(filesPath);
		File[] files = file.listFiles();
		ZipOutputStream out = null;
		FileInputStream fis = null;
		byte[] buffer = new byte[1024];
		try {
			//生成zip文件
			if (files.length > 0) {
				File zipFile = new File(zipPath);
				if (zipFile.exists() && zipFile.isFile()) {
					zipFile.delete();
				}
				out = new ZipOutputStream(new FileOutputStream(zipPath));
				for (File zipModuleFile : files) {
					fis = new FileInputStream(zipModuleFile);
					out.putNextEntry(new ZipEntry(zipModuleFile.getName()));
					int len;
					while ((len = fis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.closeEntry();
					fis.close();
				}
				out.close();
			}
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fis);
		}
	}

	/**
	 * 将指定的文件夹内容打成zip包，zip包名称为jobname
	 *
	 * @param filesPath
	 * @param jobName
	 * @return
	 * @throws Exception
	 */
	public String createZipByJobName(String filesPath, String jobName) throws Exception {
		File file = new File(filesPath);
		File[] files = file.listFiles();
		ZipOutputStream out = null;
		FileInputStream fis = null;
		byte[] buffer = new byte[1024];
		try {
			//生成zip文件，文件中除了end.job文件，应该还存在其他job文件才能打成zip包，否则返回null值。
			if (files.length > 1) {
				String zipPath = zipFilePath + File.separator + jobName + ".zip";
				File zipFile = new File(zipPath);
				if (zipFile.exists() && zipFile.isFile()) {
					zipFile.delete();
				}
				out = new ZipOutputStream(new FileOutputStream(zipPath));
				for (File zipModuleFile : files) {
					fis = new FileInputStream(zipModuleFile);
					out.putNextEntry(new ZipEntry(zipModuleFile.getName()));
					int len;
					while ((len = fis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.closeEntry();
					fis.close();
				}
				out.close();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("createZipByJobName", e);
			throw new RuntimeException(e.getMessage());
		} finally {
			deleteFile(filesPath);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fis);
		}
		return jobName + ".zip";
	}

	/**
	 * 删除文件
	 *
	 * @param filePath
	 */
	public void deleteFile(String filePath) {
		try {

			File file = new File(filePath);
			if (file.isFile()) {
				file.delete();
			} else {
				File[] childFilePath = file.listFiles();
				for (File childFile : childFilePath) {
					deleteFile(childFile.getAbsolutePath());
				}
				file.delete();
			}
		} catch (Exception e) {
			logger.error("删除文件失败", e);
		}
	}


	public static void main(String[] args) {
		try {
			ETLJob etlJob = new ETLJob();
			etlJob.setId(1);
			etlJob.setJobName("ETL定时作业");
			CreateZipFile c = new CreateZipFile();
			//c.createZipFileByJobId(etlJob);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
