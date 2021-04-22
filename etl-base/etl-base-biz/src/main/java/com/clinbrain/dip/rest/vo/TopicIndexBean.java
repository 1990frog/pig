package com.clinbrain.dip.rest.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用于主页统计分析显示
 * Created by Liaopan on 2021-04-20.
 */
@Data
@Builder
public class TopicIndexBean {

	private long total;

	private long totalOfFailure;

	private long totalOfOvertime;

	private long totalOfRunning;

	private long totalOfRange;

}
