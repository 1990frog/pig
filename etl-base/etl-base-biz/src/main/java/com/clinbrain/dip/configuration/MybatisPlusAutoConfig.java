package com.clinbrain.dip.configuration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.google.common.collect.Lists;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Liaopan on 2020-09-07.
 */

public class MybatisPlusAutoConfig {

	@Bean
	public ResourceLoader resourceLoader() {
		return new DefaultResourceLoader();
	}

	@Bean
	public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(DataSource dataSource,
																	 MybatisProperties mybatisProperties,
																	 ResourceLoader resourceLoader,
																	 Interceptor[] interceptors) {
		MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setVfs(SpringBootVFS.class);
		if(StringUtils.hasText(mybatisProperties.getConfigLocation())) {
			bean.setConfigLocation(resourceLoader.getResource(mybatisProperties.getConfigLocation()));
		}
		bean.setConfiguration(((MybatisConfiguration)mybatisProperties.getConfiguration()));
		// 设置mybatis 插件
		List<Interceptor> list = new ArrayList<>();
		if(!ObjectUtils.isEmpty(interceptors)) {
			list.addAll(Lists.newArrayList(interceptors));
		}
		// 分页插件
		list.add(new PaginationInterceptor());

		bean.setPlugins(list.toArray(new Interceptor[1]));

		if(StringUtils.hasLength(mybatisProperties.getTypeAliasesPackage())) {
			bean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
		}

		if(StringUtils.hasLength(mybatisProperties.getTypeHandlersPackage())) {
			bean.setTypeHandlersPackage(mybatisProperties.getTypeHandlersPackage());
		}

		if(ObjectUtils.isEmpty(mybatisProperties.resolveMapperLocations())) {
			bean.setMapperLocations(mybatisProperties.resolveMapperLocations());
		}

		return bean;
	}
}
