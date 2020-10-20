package com.clinbrain.dip.strategy.util;

import com.clinbrain.dip.strategy.bean.CronScheduleModelDto;

/**
 * @ClassName: CronUtil
 * @Description: Cron表达式工具类
 * 目前支持三种常用的cron表达式
 * 1.每天的某个时间点执行 例:12 12 12 * * ?表示每天12时12分12秒执行
 * 2.每周的哪几天执行         例:12 12 12 ? * 1,2,3表示每周的周1周2周3 ,12时12分12秒执行
 * 3.每月的哪几天执行         例:12 12 12 1,21,13 * ?表示每月的1号21号13号 12时12分12秒执行
 * @author lianglele
 * @date 2020-09-24 10:00
 */
public class CronUtil {

	/**
	 *
	 *方法摘要：构建Cron表达式
	 *@param  cronScheduleModelDto
	 *@return String
	 */
	public static String createCronExpression(CronScheduleModelDto cronScheduleModelDto){
		StringBuffer cronExp = new StringBuffer("");

		if(null == cronScheduleModelDto.getJobType()) {
			System.out.println("执行周期未配置" );//执行周期未配置
		}

		if (null != cronScheduleModelDto.getSecond()
			&& null != cronScheduleModelDto.getMinute()
			&& null != cronScheduleModelDto.getHour()) {

			if(cronScheduleModelDto.getJobType().intValue() != 4) {
				//秒
				cronExp.append(cronScheduleModelDto.getSecond()).append(" ");
				//分
				cronExp.append(cronScheduleModelDto.getMinute()).append(" ");
				//小时
				cronExp.append(cronScheduleModelDto.getHour()).append(" ");
			}
			//每天
			if(cronScheduleModelDto.getJobType().intValue() == 1){
				cronExp.append("* ");//日
				cronExp.append("* ");//月
				cronExp.append("?");//周
			}

			//按每周
			else if(cronScheduleModelDto.getJobType().intValue() == 3){
				//一个月中第几天
				cronExp.append("? ");
				//月份
				cronExp.append("* ");
				//周
				Integer[] weeks = cronScheduleModelDto.getDayOfWeeks();
				for(int i = 0; i < weeks.length; i++){
					if(i == 0){
						cronExp.append(weeks[i]);
					} else{
						cronExp.append(",").append(weeks[i]);
					}
				}

			}

			//按每月
			else if(cronScheduleModelDto.getJobType().intValue() == 2){
				//一个月中的哪几天
				Integer[] days = cronScheduleModelDto.getDayOfMonths();
				for(int i = 0; i < days.length; i++){
					if(i == 0){
						cronExp.append(days[i]);
					} else{
						cronExp.append(",").append(days[i]);
					}
				}
				//月份
				cronExp.append(" * ");
				//周
				cronExp.append("?");
			}
			//间隔
			else if(cronScheduleModelDto.getJobType().intValue() == 4){
				Integer h = cronScheduleModelDto.getHour();
				Integer m = cronScheduleModelDto.getMinute();
				Integer s = cronScheduleModelDto.getSecond();
				if(s == 0){
					cronExp.append(s);
				} else{
					cronExp.append("*/").append(s);
				}
				cronExp.append(" ");

				if(m == 0){
					cronExp.append(m);
				} else{
					cronExp.append("*/").append(m);
				}
				cronExp.append(" ");

				if(h == 0){
					cronExp.append(h);
				} else{
					cronExp.append("0/").append(h);
				}
				cronExp.append(" ");
				cronExp.append("* ");//日
				cronExp.append("* ");//月
				cronExp.append("?");//周
			}

		}
		else {
			System.out.println("时或分或秒参数未配置" );//时或分或秒参数未配置
		}
		return cronExp.toString();
	}

	/**
	 *
	 *方法摘要：生成计划的详细描述
	 *@param  cronScheduleModelDto
	 *@return String
	 */
	public static String createDescription(CronScheduleModelDto cronScheduleModelDto){
		StringBuffer description = new StringBuffer("");
		//计划执行开始时间
//      Date startTime = taskScheduleModel.getScheduleStartTime();

		if (null != cronScheduleModelDto.getSecond()
			&& null != cronScheduleModelDto.getMinute()
			&& null != cronScheduleModelDto.getHour()) {
			//按每天
			if(cronScheduleModelDto.getJobType().intValue() == 1){
				description.append("每天");
				description.append(cronScheduleModelDto.getHour()).append("时");
				description.append(cronScheduleModelDto.getMinute()).append("分");
				description.append(cronScheduleModelDto.getSecond()).append("秒");
				description.append("执行");
			}

			//按每周
			else if(cronScheduleModelDto.getJobType().intValue() == 3){
				if(cronScheduleModelDto.getDayOfWeeks() != null && cronScheduleModelDto.getDayOfWeeks().length > 0) {
					String days = "";
					for(int i : cronScheduleModelDto.getDayOfWeeks()) {
						days += "周" + i;
					}
					description.append("每周的").append(days).append(" ");
				}
				if (null != cronScheduleModelDto.getSecond()
					&& null != cronScheduleModelDto.getMinute()
					&& null != cronScheduleModelDto.getHour()) {
					description.append(",");
					description.append(cronScheduleModelDto.getHour()).append("时");
					description.append(cronScheduleModelDto.getMinute()).append("分");
					description.append(cronScheduleModelDto.getSecond()).append("秒");
				}
				description.append("执行");
			}

			//按每月
			else if(cronScheduleModelDto.getJobType().intValue() == 2){
				//选择月份
				if(cronScheduleModelDto.getDayOfMonths() != null && cronScheduleModelDto.getDayOfMonths().length > 0) {
					String days = "";
					for(int i : cronScheduleModelDto.getDayOfMonths()) {
						days += i + "号";
					}
					description.append("每月的").append(days).append(" ");
				}
				description.append(cronScheduleModelDto.getHour()).append("时");
				description.append(cronScheduleModelDto.getMinute()).append("分");
				description.append(cronScheduleModelDto.getSecond()).append("秒");
				description.append("执行");
			}

			//按间隔
			else if(cronScheduleModelDto.getJobType().intValue() == 4){
				description.append("间隔");
				description.append(cronScheduleModelDto.getHour()).append("时");
				description.append(cronScheduleModelDto.getMinute()).append("分");
				description.append(cronScheduleModelDto.getSecond()).append("秒");
				description.append("执行");
			}

		}
		return description.toString();
	}

	//参考例子
	public static void main(String[] args) {
		//执行时间：每天的12时12分12秒 start
		CronScheduleModelDto taskScheduleModel = new CronScheduleModelDto();
		taskScheduleModel.setJobType(4);//按每天
		Integer hour = 1; //时
		Integer minute = 30; //分
		Integer second = 0; //秒
		taskScheduleModel.setHour(hour);
		taskScheduleModel.setMinute(minute);
		taskScheduleModel.setSecond(second);
		String cropExp = createCronExpression(taskScheduleModel);
		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));
		//执行时间：每天的12时12分12秒 end

		taskScheduleModel.setJobType(3);//每周的哪几天执行
		Integer[] dayOfWeeks = new Integer[3];
		dayOfWeeks[0] = 1;
		dayOfWeeks[1] = 2;
		dayOfWeeks[2] = 3;
		taskScheduleModel.setDayOfWeeks(dayOfWeeks);
		cropExp = createCronExpression(taskScheduleModel);
		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));

		taskScheduleModel.setJobType(2);//每月的哪几天执行
		Integer[] dayOfMonths = new Integer[3];
		dayOfMonths[0] = 1;
		dayOfMonths[1] = 21;
		dayOfMonths[2] = 13;
		taskScheduleModel.setDayOfMonths(dayOfMonths);
		cropExp = createCronExpression(taskScheduleModel);
		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));

	}
}
