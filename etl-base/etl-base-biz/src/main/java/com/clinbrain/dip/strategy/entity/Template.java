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
 * (TTemplet)表实体类
 *
 * @author Liaopan
 * @since 2020-09-04 09:54:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_template")
@SuppressWarnings("serial")
public class Template {
	//模板编码，主键
	@Id
	private String code;
	//任务组名
	private String tmplName;
	//别名
	private String aliasName;
	//厂商
	private String vendor;
	//系统
	private String system;
	//数据库类型（oracle,sqlserver,cache）
	private String dbtype;
	//本地版本，用来区别本地的修改
	private String edition;
	// 本地小版本
	private String subVersion;

	private String description;

	private Boolean enable;

	/**
	 * 文件保存地址
	 */
	private String tmplPath;

	private Boolean custom;

	@JsonDeserialize(
		using = DefaultDateNullValueDeserializer.class
	)
	private Date createdAt;

	@JsonDeserialize(
		using = DefaultDateNullValueDeserializer.class
	)
	private Date updatedAt;


	public Template(String tmplName, String aliasName, String vendor, String system, String dbtype, String edition, String tmplPath) {
		this.tmplName = tmplName;
		this.aliasName = aliasName;
		this.vendor = vendor;
		this.system = system;
		this.dbtype = dbtype;
		this.edition = edition;
		this.tmplPath = tmplPath;
	}
}
