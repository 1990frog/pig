package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.connection.BeelineHiveClient;
import com.clinbrain.dip.connection.DatabaseMeta;
import com.clinbrain.dip.connection.DatabaseTableMeta;
import com.clinbrain.dip.connection.DatabaseTableMetaBuilder;
import com.clinbrain.dip.pojo.EtlHistablePartitionsConfiguration;
import com.clinbrain.dip.pojo.KylinProject;
import com.clinbrain.dip.pojo.KylinProjectSchema;
import com.clinbrain.dip.rest.mapper.DBETLManagedTableMappingMapper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liaopan on 2017/12/6.
 */
@Service
@Log4j
public class HiveService {

    @Autowired
    private BeelineHiveClient hiveClient;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DBETLManagedTableMappingMapper managedTableMappingMapper;

    /**
     * 根据database.table.column获取对应的数据类型
     *
     * @param tableName == database.table
     * @return
     */
    @Cacheable(cacheNames = "hiveTables")
    public List<DatabaseTableMeta.DatabaseTableColumnMeta> getAllColumnsByTableName(String tableName) {
        if (StringUtils.isNotBlank(tableName)
                && StringUtils.containsIgnoreCase(tableName, ".")) {
            String database = StringUtils.substringBefore(tableName, ".");
            String table = StringUtils.substringAfter(tableName, ".");
            try {
                DatabaseTableMeta tableMeta = hiveClient.getTableMeta(database, table);
                if (tableMeta != null) {
                    return tableMeta.allColumns;
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    public String getColumnDataTypeByName(String tableName, String columnName) {

        List<DatabaseTableMeta.DatabaseTableColumnMeta> columns = getAllColumnsByTableName(tableName);

        if (columns != null && StringUtils.isNotBlank(columnName)) {
            DatabaseTableMeta.DatabaseTableColumnMeta columnMeta =
                    columns.stream().filter(column -> column.name.equalsIgnoreCase(columnName)).findAny().orElse(null);
            if (columnMeta != null) {
                return columnMeta.dataType;
            }
        }

        return "";
    }

    public List<DatabaseMeta> getHiveDbInfos() {
        List<DatabaseMeta> result = new ArrayList<>();
        try {
            List<String> dbNames = hiveClient.getDbNames();
            for (String s : dbNames) {
                List<String> tables = hiveClient.getTableNames(s);
                result.add(new DatabaseMeta(s,tables));
            }
        } catch (Exception e) {
            log.error(e);
        }
        return result;

    }
    
    public List<DatabaseTableMeta> getTableDescByDataBase(int proId) {
        KylinProject project = projectService.GetDataBaseByProName(proId);
        List<DatabaseTableMeta> list = new ArrayList<>();
        for (int i = 0; i < project.getSchemas().size(); i++) {
            KylinProjectSchema schema = project.getSchemas().get(i);
            String projectDatabaseName = schema.getSchemaName();
            try {
                List<String> databaseNames = hiveClient.getTableNames(projectDatabaseName);
                for (int j = 0, k = databaseNames.size(); j < k; j++) {
                    DatabaseTableMetaBuilder hiveTable = new DatabaseTableMetaBuilder();
                    String databaseName = databaseNames.get(j);
                    hiveTable.setDatabase(projectDatabaseName.toUpperCase());
                    hiveTable.setTableName(databaseName.toUpperCase());
                    /*hiveTable.setHiveTableMeta(this.getHiveTableMeta(projectDatabaseName,databaseName));*/
                    list.add(hiveTable.createDatabaseTableMeta());
                }
            } catch (Exception e) {
                log.error(e);
            }

        }
        return list;
    }

    public List<DatabaseTableMeta.DatabaseTableColumnMeta> getHiveTableMeta(String database, String table) {
        try {
            DatabaseTableMeta tableMeta = hiveClient.getTableMeta(database, table);
            if (tableMeta != null) {
                return tableMeta.allColumns;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public List<EtlHistablePartitionsConfiguration> getTableMapping(String hospitalCode, String dbName){

        Example example = new Example(EtlHistablePartitionsConfiguration.class);
        example.createCriteria().andEqualTo("hospitalNo", hospitalCode)
                .andEqualTo("hisDbName", dbName);
        return managedTableMappingMapper.selectByExample(example);
    }

}
