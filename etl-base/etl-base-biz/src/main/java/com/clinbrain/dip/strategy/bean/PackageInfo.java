package com.clinbrain.dip.strategy.bean;

import com.clinbrain.dip.strategy.constant.SystemConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
	private String subVersion;

	/**
	 * 描述
	 */
	private String description;
}
