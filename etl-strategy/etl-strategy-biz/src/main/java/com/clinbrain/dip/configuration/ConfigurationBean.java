package com.clinbrain.dip.configuration;

import com.clinbrain.dip.connection.BeelineHiveClient;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Created by Liaopan on 2018/4/13.
 */
@Configuration
@EnableAsync
public class ConfigurationBean {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBean.class);

    @Value(value = "${hive.url}")
    private String hiveUrl;
    @Value(value = "${hive.username}")
    private String hiveUserName;
    @Value(value = "${hive.password}")
    private String hivePassword = "";

    /**
     * 如果需要连接hive ,可以打开这里的注释
     *
     * @return
     */
    @Bean
    public BeelineHiveClient beelineHiveClient(){
        BeelineHiveClient hiveClient = null;
        try {
            hiveClient = new BeelineHiveClient(hiveUrl, hiveUserName, hivePassword);
        }catch (Exception e){
            logger.error("init BeelineHiveClient error",e);
        }
        return hiveClient;
    }

    /**
     * 增加数据库厂商提供商，在mapper文件中通过databaseId 来区分不同的数据库
     * sqlserver 分页: offset ${offset} ROWS FETCH NEXT ${limit-offset} ROWS ONLY 必须有order by
     *
     * @return
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider(){
        VendorDatabaseIdProvider vendorDatabaseIdProvider =  new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("Oracle","oracle");
        properties.setProperty("Microsoft SQL Server","mssql");
        properties.setProperty("MySQL","mysql");
        vendorDatabaseIdProvider.setProperties(properties);
        return vendorDatabaseIdProvider;
    }

    @Bean("taskExecutor")
    public Executor taskExecutro(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setQueueCapacity(200);
        taskExecutor.setKeepAliveSeconds(3600*12);
        taskExecutor.setThreadNamePrefix("module-exec--");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(1800);
        return taskExecutor;
    }
}
