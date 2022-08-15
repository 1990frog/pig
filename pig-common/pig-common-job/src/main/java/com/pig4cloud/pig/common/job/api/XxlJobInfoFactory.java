package com.pig4cloud.pig.common.job.api;

/**
 * <p>
 * 封装 xxl-job 信息
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/8/14
 */
public class XxlJobInfoFactory {

	/**
	 * 工厂方法
	 * @param clin_job
	 * @return
	 */
	public static XxlJobInfo createHqmsJobInfo(CLIN_JOB clin_job){
		XxlJobInfo xxlJobInfo = new XxlJobInfo();
		xxlJobInfo.setJobGroup(clin_job.getJobGroup());
		xxlJobInfo.setAuthor(clin_job.getAuthor());
		xxlJobInfo.setScheduleType(clin_job.getScheduleType());
		xxlJobInfo.setExecutorHandler(clin_job.getExecutorHandler());
		xxlJobInfo.setExecutorRouteStrategy(clin_job.getExecutorRouteStrategy());
		xxlJobInfo.setMisfireStrategy(clin_job.getMisfireStrategy());
		xxlJobInfo.setGlueType(clin_job.getGlueType());
		xxlJobInfo.setExecutorBlockStrategy(clin_job.getExecutorBlockStrategy());
		xxlJobInfo.setTriggerStatus(clin_job.getTriggerStatus());
		return xxlJobInfo;
	}
}
