package com.clinbrain.dip.strategy.constant;

/**
 * Created by Liaopan on 2020/8/19 0019.
 */
public interface TacticsConstant {

	/**
	 * 策略包 redis 缓存键
	 */
	String PACKAGE_REDIS_KEY = "etl:package:list";

	/**
	 * 策略包后缀名
	 */
	String PACKAGE_NAME_SUFFIX = ".clb";

	/**
	 * 压缩文件的密码
	 */
	String PACKAGE_PASSWORD = "clinbrain123";

	/**
	 * 系统信息文件名称
	 */
	String PACKAGE_SYSTEM_INFO = "system";

	String TEMPLATE_DESC_SPLIT = "|||";

	String TEMPLATE_DESC_DATA_SPLIT = "|~|";
}
