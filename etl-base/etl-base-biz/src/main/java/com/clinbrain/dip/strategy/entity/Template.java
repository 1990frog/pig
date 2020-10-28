package com.clinbrain.dip.strategy.entity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.clinbrain.dip.jackson.DefaultDateNullValueDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.TEMPLATE_DESC_DATA_SPLIT;
import static com.clinbrain.dip.strategy.constant.TacticsConstant.TEMPLATE_DESC_SPLIT;

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

	//private String uuid;

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
	private Integer subVersion;

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

	/**
	 * 获取job编辑历史
	 * @return
	 */
	public List<History> getEditHistory() {
		if(StringUtils.isNotEmpty(this.description) && StrUtil.contains(this.description,TEMPLATE_DESC_SPLIT)) {
			return Arrays.stream(StrUtil.split(this.description, TEMPLATE_DESC_SPLIT)).map(s -> {
				String[] st = StrUtil.split(s, TEMPLATE_DESC_DATA_SPLIT);
				return new History(DateUtil.parse(Optional.ofNullable(st[0]).orElse(DateUtil.now())), StringUtils.defaultIfEmpty(st[1], ""));
			}).collect(Collectors.toList());
		}

		return Lists.newArrayList(new History(this.createdAt, this.description));
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class History {
		private Date datetime;
		private String desc;
	}
}
