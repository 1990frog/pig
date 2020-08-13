package com.clinbrain.dip.rest.service;


import com.alibaba.druid.sql.SQLUtils;
import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.DatabaseMeta;
import com.clinbrain.dip.connection.DatabaseTableMeta;
import com.clinbrain.dip.connection.DatabaseTableMetaBuilder;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.connection.JdbcConnectionManager;
import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.rest.mapper.DBETLConnectionMapper;
import com.clinbrain.dip.rest.mapper.DBETLDataxflowMapper;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ConnectionService extends BaseService<ETLConnection> {

    private Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    @Autowired
    private DBETLConnectionMapper connectionMapper;

    @Autowired
    private DBETLModuleMapper etlModuleMapper;

    @Autowired
    private DBETLDataxflowMapper flowMapper;

    public int getDateBasesCount(ETLConnection dipConnection) {
        try {
            IDatabaseClient databaseClient = DatabaseClientFactory.getDatabaseClient(dipConnection.getUrl(), dipConnection.getUser(), dipConnection.getPassword());
            if (StringUtils.containsIgnoreCase(dipConnection.getUrl(), DatabaseClientFactory.DatabaseProduct.ORACLE.name())){
                databaseClient.executeSQL("select 1 from dual");
            }else if(StringUtils.containsIgnoreCase(dipConnection.getUrl(), DatabaseClientFactory.DatabaseProduct.HBASE.name())) {
                return 1;
            }else {
                databaseClient.executeSQL("select 1");
            }
            databaseClient.close();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return 0;
        }
    }

    @Cacheable(cacheNames = "hiveTables")
    public List<DatabaseMeta> getDataBases(String connectionCode, String dbName, String tableName) {
        if (StringUtils.isEmpty(connectionCode)) {
            return null;
        }
        ETLConnection connection = new ETLConnection();
        connection.setConnectionCode(connectionCode);
        connection = mapper.selectByPrimaryKey(connection);
        try (IDatabaseClient databaseClient = DatabaseClientFactory.getDatabaseClient(connection.getUrl(), connection.getUser(), connection.getPassword()))
        {
                if (StringUtils.isEmpty(dbName)) {
                    List<String> dbs = databaseClient.getDbNames();
                    List<DatabaseMeta> list = Lists.newArrayList();
                    dbs.forEach(item -> {
                        DatabaseMeta meta = new DatabaseMeta();
                        meta.setName(item);
                        list.add(meta);
                    });
                    return list;
                } else if (StringUtils.isNotEmpty(dbName) && StringUtils.isEmpty(tableName)) {
                    List<DatabaseMeta> tableList = Lists.newArrayList();
                    DatabaseMeta databases = new DatabaseMeta();
                    logger.debug("startGetDBInfos:" + System.currentTimeMillis() / 1000);
                    //dbs.forEach(dbName -> {
                    try {
                        logger.debug("dbName get all table names：" + dbName);
                        List<String> tables = databaseClient.getTableNames(dbName);
                        List<DatabaseTableMeta> tableMetas = new ArrayList<>();
                        tables.forEach(table -> {
                            tableMetas.add(new DatabaseTableMetaBuilder().setTableName(table).setAllColumns(null).createDatabaseTableMeta());
                        });
                        databases.setName(dbName);
                        databases.setTableMetas(tableMetas);
                        tableList.add(databases);
                        return tableList;
                    } catch (Exception e) {
                        logger.error("查询数据库对应表信息出错", e);
                    }
                    logger.debug("end: " + System.currentTimeMillis() / 1000);
                } else {
                    List<DatabaseMeta> cloumnList = Lists.newArrayList();
                    DatabaseMeta databaseMeta = new DatabaseMeta();
                    List<DatabaseTableMeta> tableMetas = new ArrayList<>();
                    DatabaseTableMetaBuilder tableMeta = new DatabaseTableMetaBuilder();
                    DatabaseTableMeta tableTemp = databaseClient.getTableMeta(dbName, tableName);
                    tableMetas.add(tableTemp);
                    databaseMeta.setName(dbName);
                    databaseMeta.setTableMetas(tableMetas);
                    cloumnList.add(databaseMeta);
                    return cloumnList;
                }
        } catch (Exception e) {
            logger.error("查询数据库出错", e);
        }
        return null;
    }

    public boolean putConnection(ETLConnection connection) {
        ETLConnection conn = connectionMapper.selectByPrimaryKey(connection.getConnectionCode());
        if (conn.getUrl() != connection.getUrl() || conn.getUser() != conn.getUser() || conn.getPassword() != conn.getPassword()) {
            JdbcConnectionManager.clearDataSourcePool(connection.getUrl() + "|" + connection.getUser() + "|" + connection.getPassword());
        }
        return connectionMapper.updateByPrimaryKeySelective(connection) > 0;
    }

    public boolean appendConnection(ETLConnection connection) {
        connection.setCreatedAt(new Date());
        connection.setUpdatedAt(new Date());
        return connectionMapper.insert(connection) > 0;
    }

    public String validSql(String connectionCode, String sql) {
        try {
            SQLUtils.parseStatements(sql, connectionCode);
        } catch (Exception e) {
            logger.error("validSql出错", e);
            return e.getMessage();
        }
        return null;
    }

    public int checkConnectionByModule(String connectionCode) {
        return etlModuleMapper.checkConnectionByModule(connectionCode);
    }

    @Cacheable(cacheNames = "hiveTableColumns")
    public DatabaseMeta getDataBases(String connectionCode, String dbName) {
        if (StringUtils.isEmpty(connectionCode)) {
            return null;
        }
        ETLConnection connection = new ETLConnection();
        connection.setConnectionCode(connectionCode);
        connection = mapper.selectByPrimaryKey(connection);

        IDatabaseClient databaseClient = DatabaseClientFactory.getDatabaseClient(connection.getUrl(), connection.getUser(), connection.getPassword());
        try {
            List<String> tables = databaseClient.getTableNames(dbName);
            DatabaseMeta databaseMeta = new DatabaseMeta();
            List<DatabaseTableMeta> tableMetas = new ArrayList<>();

            tables.forEach(table -> {
                try {
                    DatabaseTableMeta tableColumn = getTableMeta(databaseClient, dbName, table);
                    tableMetas.add(tableColumn);
                } catch (Exception e) {
                    logger.error("读取数据库表结构出错", e);
                }
            });

            databaseMeta.setName(dbName);
            databaseMeta.setTableMetas(tableMetas);
            return databaseMeta;
        } catch (Exception e) {
            logger.error("getDataBases出错", e);
        }
        return null;
    }

    @Cacheable(cacheNames = "databaseTableColumns")
    public DatabaseTableMeta getTableMeta(IDatabaseClient client, String dbName, String table) throws Exception {
        return client.getTableMeta(dbName, table);
    }
}
