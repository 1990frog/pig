package com.clinbrain.dip.strategy.bean;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.clinbrain.dip.strategy.constant.SystemConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * 定义策略模板包属性
 * Created by Liaopan on 2020/8/14 0014.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageInfo implements Serializable {


	private static final long serialVersionUID = -1828157974290454842L;

	public PackageInfo(String name, String vendor, String system) {
		this.name = name;
		this.vendor = vendor;
		this.system = system;
	}

	public String getCode() {
		return StrUtil.format("{}_{}{}_{}_{}", PinyinUtil.getFirstLetter(this.vendor, ""),
			this.system, this.edition, PinyinUtil.getFirstLetter(this.name, ""),
			this.subVersion);
	}

	public Integer addSubVersion() {
		return BigDecimal.valueOf(this.subVersion).add(BigDecimal.ONE).intValue();
	}

	private String id;
	/**
	 * 名称
	 */
	private String name;

	/**
	 * 厂商
	 */
	private String vendor;

	/**
	 * 系统（HIS,RIS）
	 */
	private String system;

	/**
	 * 版本
	 */
	private String edition;

	/**
	 * 小版本号，一般由我们自己定义
	 */
	private Integer subVersion = 1;

	/**
	 * 描述
	 */
	private String desc;

	/**
	 * 关联的模板ID
	 */
	private String preTemplateId;

	/**
	 * 调度时间
	 */
	private String cron;

	/**
	 * 调度时间描述
	 */
	private String cronDesc;
}
