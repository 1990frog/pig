/*
package com.clinbrain.dip.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.clinbrain.dip.common.DipConfig;
import com.pig4cloud.pig.common.job.annotation.EnablePigXxlJob;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

*/
/**
 * Created by Liaopan on 2021-01-28.
 *//*

@Component
@EnablePigXxlJob
@Slf4j
public class EtlLogClearScheduler {

*/
/**
	 * 每天0点执行清除etl log文件
	 *//*

	@XxlJob("clearETLLog")
	public ReturnT<String> clearLog(String param) {
		final DipConfig configInstance = DipConfig.getConfigInstance();
		final String logbackDir = configInstance.getProperty("logback.dir", "/logs");
		final int maxHistory = Integer.parseInt(configInstance.getProperty("logback.maxHistory", "60"));
		log.info("准备清除etl系统运行日志，日志保存路径: {}, 最大保存时间(天)：{}", logbackDir, maxHistory);
		final DateTime currentDate = DateUtil.date();
		Stream.of(FileUtil.ls(logbackDir)).forEach(file -> {
			final DateTime fileDate = DateUtil.parse(file.getName());
			if(DateUtil.between(currentDate, fileDate , DateUnit.DAY) > maxHistory) {
				log.info("准备删除{}目录下的日志文件", file.getPath());
				try {
					FileUtil.del(file.getPath());
				}catch (Exception e) {
					log.error("删除文件失败{}", file.getPath() , e);
				}
			}
		});
		return ReturnT.SUCCESS;
	}

}
*/
