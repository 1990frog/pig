package com.clinbrain.dip.strategy.util;

import com.alibaba.fastjson.JSON;
import com.clinbrain.dip.strategy.bean.CronScheduleModelDto;

import java.util.Arrays;

/**
 * @author lianglele
 * @ClassName: CronUtil
 * @Description: Cron表达式工具类
 * 目前支持三种常用的cron表达式
 * 1.每天的某个时间点执行 例:12 12 12 * * ?表示每天12时12分12秒执行
 * 2.每周的哪几天执行         例:12 12 12 ? * 1,2,3表示每周的周1周2周3 ,12时12分12秒执行
 * 3.每月的哪几天执行         例:12 12 12 1,21,13 * ?表示每月的1号21号13号 12时12分12秒执行
 * @date 2020-09-24 10:00
 */
public class CronUtil {

	/**
	 * 方法摘要：构建Cron表达式
	 *
	 * @param cronScheduleModelDto
	 * @return String
	 */
	public static String createCronExpression(CronScheduleModelDto cronScheduleModelDto) {
		StringBuffer cronExp = new StringBuffer("");

		if (null == cronScheduleModelDto.getJobType()) {
			System.out.println("执行周期未配置");//执行周期未配置
		}

		if (null != cronScheduleModelDto.getSecond()
			&& null != cronScheduleModelDto.getMinute()
			&& null != cronScheduleModelDto.getHour()) {

			if (cronScheduleModelDto.getJobType().intValue() != 4 && cronScheduleModelDto.getJobType().intValue() != 5) {
				//秒
				cronExp.append(cronScheduleModelDto.getSecond()).append(" ");
				//分
				cronExp.append(cronScheduleModelDto.getMinute()).append(" ");
				//小时
				cronExp.append(cronScheduleModelDto.getHour()).append(" ");
			}
			//每天
			if (cronScheduleModelDto.getJobType().intValue() == 1) {

				cronExp.append(startOrEndTime(cronScheduleModelDto));//天

			}

			//按每周
			else if (cronScheduleModelDto.getJobType().intValue() == 3) {

				cronExp.append(startOrEndTime(cronScheduleModelDto));//天
			}

			//按每月
			else if (cronScheduleModelDto.getJobType().intValue() == 2) {
				//一个月中的哪几天
				cronExp.append(startOrEndTime(cronScheduleModelDto));//月
			}
			//间隔
			else if (cronScheduleModelDto.getJobType().intValue() == 4) {
//				Integer h = cronScheduleModelDto.getHour();
//				Integer m = cronScheduleModelDto.getMinute();
//				Integer s = cronScheduleModelDto.getSecond();
//				if (s == 0) {
//					cronExp.append(s);
//				} else {
//					cronExp.append("*/").append(s);
//				}
//				cronExp.append(" ");
//
//				if (m == 0) {
//					cronExp.append(m);
//				} else {
//					cronExp.append("*/").append(m);
//				}
//				cronExp.append(" ");
//
//				if (h == 0) {
//					cronExp.append(h);
//				} else {
//					cronExp.append("0/").append(h);
//				}
//				cronExp.append(" ");
				cronExp.append(cornCycle(cronScheduleModelDto));//周
				//周期
			} else if (cronScheduleModelDto.getJobType().intValue() == 5) {
				return cornCycle(cronScheduleModelDto);

			}


		} else {
			System.out.println("时或分或秒参数未配置");//时或分或秒参数未配置
		}
		return cronExp.toString();
	}


	private static String cornCycle(CronScheduleModelDto scheduleModelDto) {
		StringBuffer cronExp = new StringBuffer("");
		//天
		Integer[] days = scheduleModelDto.getDayOfMonths();

		//月
		Integer[] mons = scheduleModelDto.getDayOfMonths();

		//周
		Integer[] weeks = scheduleModelDto.getDayOfWeeks();

		//年
		Integer[] years = scheduleModelDto.getDayOfYears();

		//时
		Integer[] hours = scheduleModelDto.getStartOfEndHours();
		//分
		Integer[] minute = scheduleModelDto.getStartOfEndMinutes();

		Integer second =scheduleModelDto.getSecond();

		int type = scheduleModelDto.getJobType();
		//秒
		if (second > 0) {
			if(type == 4){
				cronExp.append("*/" + second).append(" ");
			} else {
				cronExp.append(second).append(" ");
			}

		} else {
			cronExp.append("0").append(" ");
		}

		//分
		if (minute != null) {
			if (minute.length >= 2) {
				cronExp.append(minute[0] + "-" + minute[1] + "/" + scheduleModelDto.getMinute()).append(" ");
			} else {
				cronExp.append(minute[0] + "/" + scheduleModelDto.getMinute()).append(" ");
			}
		} else {
			if(hours != null || days != null || mons != null || weeks != null || years != null){
				if(scheduleModelDto.getMinute() > 0){
					if(type == 4){
						cronExp.append("*/" + scheduleModelDto.getMinute()).append(" ");
					} else {
						cronExp.append(scheduleModelDto.getMinute()).append(" ");
					}

				} else {
					cronExp.append(0).append(" ");
				}

			} else {
				cronExp.append("*").append(" ");
			}
		}

		//时
		if (hours != null) {
			 if (scheduleModelDto.getHour() > 0 && hours.length >= 2) {
				cronExp.append(hours[0] + "-" + hours[1] + "/" + scheduleModelDto.getHour()).append(" ");
			} else if (scheduleModelDto.getHour() > 0) {
				//间隔
				cronExp.append(hours[0] + "/" + scheduleModelDto.getHour()).append(" ");
			} else {
				cronExp.append(0).append(" ");
			}
		} else {
			if(scheduleModelDto.getHour() > 0){
				if(type == 4){
					cronExp.append("*/" + scheduleModelDto.getHour()).append(" ");
				} else {
					cronExp.append(scheduleModelDto.getHour()).append(" ");
				}

			} else {
				cronExp.append("*").append(" ");
			}
		}

		// 天
		if (days != null) {
			if (scheduleModelDto.getDay() > 0 && days.length == 2) {
				cronExp.append(days[0] + "-" + days[1]+ "/" + scheduleModelDto.getDay()).append(" ");
			} else if (scheduleModelDto.getDay() > 0) {
				cronExp.append(days[0] + "/" + scheduleModelDto.getDay()).append(" ");
			} else {
				cronExp.append(days[0]).append(" ");
			}
		}else {
			if(scheduleModelDto.getDay() > 0){
				if(type == 4){
					cronExp.append("*/" + scheduleModelDto.getDay()).append(" ");
				} else {
					cronExp.append(scheduleModelDto.getDay()).append(" ");
				}
			} else {
				cronExp.append("*").append(" ");
			}
		}

		//月
		if (mons != null) {
			if (mons.length >= 2) {
				cronExp.append(mons[0] + "-" + mons[1]+ "/" + scheduleModelDto.getMonth()).append(" ");
			} else {
				//间隔
				String toMons = Arrays.toString(mons).replace("[", "").replace("]", "");
				cronExp.append(scheduleModelDto.getMonth() + "/" + toMons).append(" ");
			}
		} else {
			if(weeks != null || years != null){
				if(scheduleModelDto.getMonth() > 0){
					if(type == 4){
						cronExp.append("*/" + scheduleModelDto.getMonth()).append(" ");
					} else {
						cronExp.append(scheduleModelDto.getMonth()).append(" ");
					}
				} else {
					cronExp.append(0).append(" ");
				}

			} else {
				cronExp.append("*").append(" ");
			}
		}

		if (weeks != null) { //周
			String toWeeks = Arrays.toString(weeks).replace("[", "").replace("]", "");
			if (scheduleModelDto.getWeek() > 0 && weeks.length > 0) {
//				cronExp.append(weeks[0] + "-" + weeks[1] + "/" + scheduleModelDto.getWeek()).append(" ");
//			} else {
				//间隔
				cronExp.append(scheduleModelDto.getWeek() + "#" + toWeeks).append(" ");
			} else {
				cronExp.append(toWeeks).append(" ");
			}

		} else {
			if(scheduleModelDto.getWeek() > 0){
				cronExp.append(scheduleModelDto.getWeek()).append("L ");
			} else {
				cronExp.append("?").append(" ");
			}
		}

		if (years != null) {
			if (years.length >= 2) {
				cronExp.append(years[0] + "-" + years[1]).append(" ");
			} else {
				cronExp.append(years[0]);
			}
		}
		return cronExp.toString();
	}


	private static String startOrEndTime(CronScheduleModelDto scheduleModelDto){
		StringBuffer cronExp = new StringBuffer();
		if(scheduleModelDto.getDays()!= null && scheduleModelDto.getDays().length > 0){
			if(scheduleModelDto.getDay() > 0){
				cronExp.append(scheduleModelDto.getDay()).append("/").append(Arrays.toString(scheduleModelDto.getDays()).replace("[","").replace("]","")).append(" ");//日
			} else {
				cronExp.append(Arrays.toString(scheduleModelDto.getDays()).replace("[","").replace("]","")).append(" ");//日
			}
		} else {
			if(scheduleModelDto.getDay() > 0){
				cronExp.append(scheduleModelDto.getDay()).append(" ");//日
			} else if((scheduleModelDto.getDayOfYears()!= null && scheduleModelDto.getDayOfYears().length > 0) || (scheduleModelDto.getDayOfMonths()!= null && scheduleModelDto.getDayOfMonths().length > 0)){
				cronExp.append("0 ");//日
			} else {
				cronExp.append("* ");//日
			}
		}

		if(scheduleModelDto.getDayOfMonths()!= null && scheduleModelDto.getDayOfMonths().length > 0){
			if(scheduleModelDto.getMonth() > 0){
				cronExp.append(scheduleModelDto.getMonth()).append("/").append(Arrays.toString(scheduleModelDto.getDayOfMonths()).replace("[","").replace("]","")).append(" ");//月
			} else {
				cronExp.append(Arrays.toString(scheduleModelDto.getDayOfMonths()).replace("[","").replace("]","")).append(" ");//月
			}

		} else {
			if(scheduleModelDto.getMonth() > 0){
				cronExp.append(scheduleModelDto.getMonth()).append(" ");//月
			} else if(scheduleModelDto.getDayOfYears()!= null && scheduleModelDto.getDayOfYears().length > 0){
				cronExp.append("0 ");//月
			} else {
				cronExp.append("* ");//月
			}

		}

		if(scheduleModelDto.getDayOfWeeks()!= null && scheduleModelDto.getDayOfWeeks().length > 0){
			if(scheduleModelDto.getWeek() > 1){
				cronExp.append(scheduleModelDto.getMonth()).append("/").append(Arrays.toString(scheduleModelDto.getDayOfWeeks()).replace("[","").replace("]","")).append(" ");//周
			} else {
				cronExp.append(Arrays.toString(scheduleModelDto.getDayOfWeeks()).replace("[","").replace("]","")).append(" ");//周
			}
		} else {
			cronExp.append("? ");//周
		}


		if(scheduleModelDto.getDayOfYears()!= null && scheduleModelDto.getDayOfYears().length > 0){
			if(scheduleModelDto.getDayOfYears().length == 2){
				cronExp.append(scheduleModelDto.getDayOfYears()[0]).append("-").append(scheduleModelDto.getDayOfYears()[1]).append(" ");//年
			} else {
				cronExp.append(scheduleModelDto.getDayOfYears()[0]).append(" ");//年
			}

		}
		return cronExp.toString();
	}

	/**
	 * 方法摘要：生成计划的详细描述
	 *
	 * @param cronScheduleModelDto
	 * @return String
	 */
	public static String createDescription(CronScheduleModelDto cronScheduleModelDto) {
		StringBuffer description = new StringBuffer("");
		//计划执行开始时间
//      Date startTime = taskScheduleModel.getScheduleStartTime();

		if (null != cronScheduleModelDto.getSecond()
			&& null != cronScheduleModelDto.getMinute()
			&& null != cronScheduleModelDto.getHour()) {
			//按每天
			if (cronScheduleModelDto.getJobType().intValue() == 1) {
				description.append("每天");
				description.append(cronScheduleModelDto.getHour()).append("时");
				description.append(cronScheduleModelDto.getMinute()).append("分");
				description.append(cronScheduleModelDto.getSecond()).append("秒");
				description.append("执行");
			}

			//按每周
			else if (cronScheduleModelDto.getJobType().intValue() == 3) {
				if (cronScheduleModelDto.getDayOfWeeks() != null && cronScheduleModelDto.getDayOfWeeks().length > 0) {
					String days = "";
					for (int i : cronScheduleModelDto.getDayOfWeeks()) {
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
			else if (cronScheduleModelDto.getJobType().intValue() == 2) {
				//选择月份
				if (cronScheduleModelDto.getDayOfMonths() != null && cronScheduleModelDto.getDayOfMonths().length > 0) {
					String days = "";
					for (int i : cronScheduleModelDto.getDayOfMonths()) {
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
			else if (cronScheduleModelDto.getJobType().intValue() == 4) {
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
//		taskScheduleModel.setJobType(4);//按每天
//		Integer hour = 1; //时
//		Integer minute = 30; //分
//		Integer second = 0; //秒
//		taskScheduleModel.setHour(hour);
//		taskScheduleModel.setMinute(minute);
//		taskScheduleModel.setSecond(second);
//		String cropExp = createCronExpression(taskScheduleModel);
//		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));
//		//执行时间：每天的12时12分12秒 end
//
//		taskScheduleModel.setJobType(3);//每周的哪几天执行
//		Integer[] dayOfWeeks = new Integer[3];
//		dayOfWeeks[0] = 1;
//		dayOfWeeks[1] = 2;
//		dayOfWeeks[2] = 3;
//		taskScheduleModel.setDayOfWeeks(dayOfWeeks);
//		cropExp = createCronExpression(taskScheduleModel);
//		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));
//
//		taskScheduleModel.setJobType(2);//每月的哪几天执行
//		Integer[] dayOfMonths = new Integer[3];
//		dayOfMonths[0] = 1;
//		dayOfMonths[1] = 21;
//		dayOfMonths[2] = 13;
//		taskScheduleModel.setDayOfMonths(dayOfMonths);
//		cropExp = createCronExpression(taskScheduleModel);
//		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));


		taskScheduleModel.setJobType(3);//每月的哪几天执行
		Integer[] Months = new Integer[3]; //月
		Months[0] = 1;
		Months[1] = 3;
		Months[2] = 5;
		taskScheduleModel.setDayOfMonths(Months);
		taskScheduleModel.setMonth(0);

		Integer[] week = new Integer[1]; // 周
		week[0] = 1;
//		week[1] = 6;
//		taskScheduleModel.setDayOfWeeks(week);
		taskScheduleModel.setWeek(0);

		Integer[] year = new Integer[1]; // 年
		year[0] = 2020;
//		year[1] = 2021;
//		taskScheduleModel.setDayOfYears(year);

//		Integer[] hours = new Integer[2]; //时
//		hours[0] = 1;
//		hours[1] = 23;
//		taskScheduleModel.setStartOfEndHours(hours);
		taskScheduleModel.setHour(0);

//		Integer[] min = new Integer[2]; //分
//		min[0] = 1;
//		min[1] = 50;
//		taskScheduleModel.setStartOfEndMinutes(min);
		taskScheduleModel.setMinute(0);
		Integer[] day = new Integer[1]; //日
		day[0] = 1;
//		day[1] = 20;
		taskScheduleModel.setDays(day);
		taskScheduleModel.setDay(2);

		taskScheduleModel.setSecond(0);

		System.out.println(JSON.toJSONString(taskScheduleModel));

		String cropExp = createCronExpression(taskScheduleModel);
		System.out.println(cropExp + ":" + createDescription(taskScheduleModel));

	}
}
