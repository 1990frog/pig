package com.clinbrain.dip.strategy.bean;

import com.clinbrain.dip.pojo.ETLScheduler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lianglele
 * @date 2020-10-22 17:02
 */
@Data
@ApiModel("cron模型")
public class ETLSchedulerDto extends ETLScheduler implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 分解模型
	 */
	@ApiModelProperty("分解模型")
	private CronScheduleModelDto cromModel;
}
