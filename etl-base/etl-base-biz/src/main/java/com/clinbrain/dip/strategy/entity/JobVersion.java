package com.clinbrain.dip.strategy.entity;

import com.clinbrain.dip.jackson.DefaultDateNullValueDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * (t_version)表实体类
 *
 * @author lianglele
 * @since 2020-09-24 09:54:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_workflow_sql_version")
public class JobVersion {
	//模板编码，主键
	@Id
	private Integer id;
	//任务唯一code
	private String workflowCode;
	//版本code
	private String versionCode;

	private String workflowSql;

	private Integer userId;

	private Integer status = 0;

	@JsonDeserialize(
		using = DefaultDateNullValueDeserializer.class
	)
	private Date createDate;


}
