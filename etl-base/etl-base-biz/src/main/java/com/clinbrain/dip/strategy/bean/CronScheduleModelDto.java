package com.clinbrain.dip.strategy.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lianglele
 * @date 2020-09-24 9:58
 */
@Data
public class CronScheduleModelDto implements Serializable {
	private static final long serialVersionUID = -1828157974290454842L;
	/**
	 * 所选作业类型:
	 * 1  -> 每天
	 * 2  -> 每月
	 * 3  -> 每周
	 * 4  ->间隔（每隔2个小时，每隔30分钟）
	 * 5 -> 周期 必须有开始和结束时间
	 */
	Integer jobType;

	/**
	 * 年周期
	 */
	Integer[] dayOfYears;

	/**一周的哪几天*/
	Integer[] dayOfWeeks;

	/**一个月的哪几天*/
	Integer[] dayOfMonths;

	/**
	 * 开始结束周期 时
	 */
	Integer[] startOfEndHours;
	/**
	 * 开始结束周期 日
	 */
	Integer[] days;
	/**
	 * 开始结束周期 分
	 */
	Integer[] startOfEndMinutes;

	/**秒  */
	Integer second;

	/**分  */
	Integer minute;

	/**时  */
	Integer hour;

	/**日  每天执行 间隔1天就是每天都执行，2天是间隔一天 2--3 */
	Integer day;

	/**月  */
	Integer month;

	/**周  */
	Integer week;


}
