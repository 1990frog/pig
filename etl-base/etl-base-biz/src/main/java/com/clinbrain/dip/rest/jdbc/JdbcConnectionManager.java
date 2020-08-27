package com.clinbrain.dip.rest.jdbc;

import com.clinbrain.dip.metadata.ConnectionPoolProperty;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(JdbcConnectionManager.class);
    private static final Map<String, BasicDataSource> dataSourcePool = new ConcurrentHashMap();

    public static BasicDataSource getMondrianJdbcDataSource(ConnectionPoolProperty properties) throws ClassNotFoundException {
        BasicDataSource r = dataSourcePool.get(properties.getType().getName());
        if (r == null) {
            synchronized (JdbcConnectionManager.class) {
                if (r == null) {
                    BasicDataSource dataSource = new BasicDataSource();
                    Class.forName(properties.getDriverName());
                    dataSource.setDriverClassName(properties.getDriverName());
                    dataSource.setUrl(properties.getUrl());
                    dataSource.setMaxTotal(properties.getMaxTotal());
                    dataSource.setMaxIdle(properties.getMaxIdle());
                    dataSource.setMinIdle(properties.getMinIdle());
                    // Default settings
                    if (properties.getType() != ConnectionPoolProperty.ConnectionType.MOLAP &&
                            properties.getType() != ConnectionPoolProperty.ConnectionType.ROLAP) {
                        dataSource.setTestOnBorrow(true);
                        dataSource.setValidationQuery("select 1");
                    }
                    dataSource.setRemoveAbandonedOnBorrow(true);
                    dataSource.setRemoveAbandonedOnMaintenance(true);
                    dataSource.setRemoveAbandonedTimeout(300);
                    r = dataSource;
                    dataSourcePool.put(properties.getType().getName(), r);
                }
            }
        }
        return r;
    }

    private JdbcConnectionManager() {
    }
}
