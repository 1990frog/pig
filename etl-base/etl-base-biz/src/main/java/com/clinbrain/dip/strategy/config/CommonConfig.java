package com.clinbrain.dip.strategy.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Liaopan on 2020/8/14 0014.
 */
@Configuration
@Data
@ToString
@RefreshScope
public class CommonConfig {

	/**
	 * 存放策略包的路径地址
	 */
	@Value("${package.path}")
	private String packagePath;

	/**
	 *  zip 文件加密密码
	 */
	@Value("${package.code}")
	private String zipPassword;

}
