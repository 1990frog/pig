package com.pig4cloud.pig.common.job.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * xxl-job 任务配置
 * todo 引入官方 enum
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/8/14
 */
@Getter
@AllArgsConstructor
public enum CLIN_JOB {

	hqms("admin",2,"CRON","hqms","FIRST","DO_NOTHING","BEAN","SERIAL_EXECUTION",1);

	/*写死 admin*/
	private final String author;
	private final int jobGroup;
	private final String scheduleType;
	private final String executorHandler;
	private final String executorRouteStrategy;
	private final String misfireStrategy;
	private final String glueType;
	private final String executorBlockStrategy;
	private final int triggerStatus;

}
