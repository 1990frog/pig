package com.clinbrain.dip.rest.vo;

import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lianglele
 * @date 2020-10-22 17:02
 */
@Data
@ApiModel("ETL树形结构")
public class ETLJobVo extends ETLJob implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 状态
	 */
	@ApiModelProperty("状态")
	private Integer status;

	/**
	 * 任务
	 */
	@ApiModelProperty("任务")
	private List<ETLModule> moduleList;
}
