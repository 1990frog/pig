package com.clinbrain.dip.strategy.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by Liaopan on 2020-09-18.
 *  接受页面上模板匹配的系统和连接信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SystemConnectionCodeVO {

	private String system;
	private String connectionCode;
}
