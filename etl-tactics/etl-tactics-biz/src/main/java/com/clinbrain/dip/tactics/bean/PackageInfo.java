package com.clinbrain.dip.tactics.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 定义策略包属性
 * Created by Liaopan on 2020/8/14 0014.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageInfo implements Serializable {


	private static final long serialVersionUID = -1828157974290454842L;
	/**
	 * 名称
	 */
	private String name;

	/**
	 * 厂商
	 */
	private String vendor;

	/**
	 * 版本
	 */
	private String version;
}
