package com.clinbrain.dip.schedule;

import com.clinbrain.dip.common.DefineLogbackDir;
import com.clinbrain.dip.common.DipConfig;
import com.clinbrain.dip.util.LogBackUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Liaopan on 2020-12-09.
 */
@Component
@EnableScheduling
@Slf4j
public class EtlLogClearScheduler {

	@Scheduled(cron = " 0 0/2 15 1/1 * ? ")
	public void clearLog() {
		final DipConfig configInstance = DipConfig.getConfigInstance();
		final String logbackDir = configInstance.getProperty("logback.dir", "/logs");
		final String maxHistoryStr = configInstance.getProperty("logback.maxHistory", "30");
		System.out.println(logbackDir);
	}
}
