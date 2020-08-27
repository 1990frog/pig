package com.clinbrain.dip.tactics.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Liaopan on 2020/8/14 0014.
 */
@Configuration
@Data
@ToString
@ConfigurationProperties(prefix = "common")
public class CommonConfig {

	@Value("${package.path}")
	private String packagePath;
}
