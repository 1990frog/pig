package com.clinbrain.dip.strategy.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * (THospitalSystem)实体类
 *
 * @author Liaopan
 * @since 2020-10-30 16:04:25
 */
@Table(name = "t_hospital_system")
@EqualsAndHashCode
@Data
@ApiModel(value = "HospitalSystem", description = "")
public class HospitalSystem implements Serializable {
	private static final long serialVersionUID = 900597644737997207L;
	@Column(name = "id")
	@ApiModelProperty(value = "id")
	private Long id;

	@Column(name = "hospital_code")
	@ApiModelProperty(value = "医院编码")
	private String hospitalCode;

	@Column(name = "vendor")
	@ApiModelProperty(value = "厂商")
	private String vendor;

	@Column(name = "system")
	@ApiModelProperty(value = "系统版本")
	private String system;


}
