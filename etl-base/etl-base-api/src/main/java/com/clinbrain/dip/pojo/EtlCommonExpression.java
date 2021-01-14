package com.clinbrain.dip.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * (EtlCommonExpression)实体类
 *
 * @author Liaopan
 * @since 2021-01-11 17:51:06
 */
@Table(name = "etl_common_expression")
@Data
@ApiModel(value = "EtlCommonExpression", description = "")
public class EtlCommonExpression extends BaseObject {
	private static final long serialVersionUID = -14428339767715369L;
	@Column(name = "id")
	@ApiModelProperty(value = "${column.comment}")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "expr_name")
	@ApiModelProperty(value = "${column.comment}")
	private String exprName;

	@Column(name = "expr_type")
	@ApiModelProperty(value = "${column.comment}")
	private String exprType;

	@Column(name = "expression")
	@ApiModelProperty(value = "${column.comment}")
	private String expression;


}
