package com.clinbrain.dip.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.util.Set;

/**
 * Created by Liaopan on 2020-12-03.
 */
@Configuration
public class NacosConfig implements ApplicationRunner {

	@Autowired(required = false)
	private NacosAutoServiceRegistration registration;

	@Value("${server.port}")
	Integer port;


	@Override
	public void run(ApplicationArguments args) throws Exception {
		if(registration != null && port != null) {
			Integer tomcatPort = port;
			try {
				tomcatPort = Integer.parseInt(getTomcatPort());
				System.out.println("fetch tomcat port:" + tomcatPort);
			}catch(Exception e) {
				System.out.println("fetch tomcat port error!" + e.getMessage());
			}
			registration.setPort(tomcatPort);
			registration.start();
		}
	}

	public String getTomcatPort() throws Exception {
		MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
			Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		return objectNames.iterator().next().getKeyProperty("port");
	}
}
