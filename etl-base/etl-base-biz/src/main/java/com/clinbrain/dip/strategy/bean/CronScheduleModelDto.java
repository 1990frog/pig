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
	 */
	Integer jobType;

	/**一周的哪几天*/
	Integer[] dayOfWeeks;

	/**一个月的哪几天*/
	Integer[] dayOfMonths;

	/**秒  */
	Integer second;

	/**分  */
	Integer minute;

	/**时  */
	Integer hour;
}
