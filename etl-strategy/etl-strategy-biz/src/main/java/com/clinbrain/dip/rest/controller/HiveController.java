package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.connection.DatabaseTableMeta;
import com.clinbrain.dip.connection.OracleClient;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.HiveService;
import com.clinbrain.dip.rest.service.ProjectService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liaopan on 2017/12/7.
 */
@RestController
@RequestMapping("/hive")
public class HiveController {
    private static final Logger logger = LoggerFactory.getLogger(HiveController.class);

    @Autowired
    private HiveService hiveService;

    @Autowired
    private ProjectService projectService;


    @RequestMapping(value = "", method =  RequestMethod.GET)
    public ResponseData getTableDescByDataBase(@RequestParam int proId){
        return new ResponseData.Builder<List>(hiveService.getTableDescByDataBase(proId)).success();
    }

    @RequestMapping(value = "columnDatatype",method = RequestMethod.GET)
    public ResponseData getColumnDataTypeByName(@RequestParam(value = "tableName") String tableName,
                                                @RequestParam(value = "column") String column){

        return new ResponseData.Builder<String>(hiveService.getColumnDataTypeByName(tableName,column)).success();
    }

    @RequestMapping(value = "columns",method = RequestMethod.GET)
    public ResponseData test(@RequestParam(value = "tableName") String tableName){
        List<DatabaseTableMeta.DatabaseTableColumnMeta> columns = new ArrayList<>();
        if(StringUtils.containsIgnoreCase(tableName,"|")){
            Lists.newArrayList(StringUtils.split(tableName,"|"))
                    .forEach(table ->columns.addAll(hiveService.getAllColumnsByTableName(table)));
            return new ResponseData.Builder<List>(columns).success();
        }else {
            return new ResponseData.Builder<List>(hiveService.getAllColumnsByTableName(tableName)).success();
        }
    }

    @RequestMapping(value = "/databases",method = RequestMethod.GET)
    public ResponseData dbinfo(){
        return new ResponseData.Builder<List>(hiveService.getHiveDbInfos()).success();
    }

    @RequestMapping(value = "/table",method = {RequestMethod.GET},produces = {"application/json"})
    public ResponseData getTableDesc(@RequestParam(value = "database") String database,@RequestParam(value = "table") String table){
        return new ResponseData.Builder<List>(hiveService.getHiveTableMeta(database,table)).success();
    }

    @GetMapping("/mappings")
    public ResponseData getMappingTables(@RequestParam(value = "hospitalCode") String hospitalCode,
                                     @RequestParam(value = "dbName") String dbName){
        return new ResponseData.Builder<List>(hiveService.getTableMapping(hospitalCode,dbName)).success();
    }

    @RequestMapping(value = "/hbase/{server}/{username}/{password}",method = {RequestMethod.GET}, produces = {"application/json"})
    public String generateHiveExternalTable(@PathVariable String server, @PathVariable String username, @PathVariable String password) throws Exception {
        String url = String.format("jdbc:oracle:thin:@%s:1521:orcl", server);
        long start = System.currentTimeMillis();
        OracleClient oracle = new OracleClient(url, username, password);
        long end1 = System.currentTimeMillis();
        logger.info(String.format("Build connection elapsed time: %d", end1 - start));
        List<DatabaseTableMeta> list = oracle.getAllTableMeta(username);
        String sql = oracle.generateHiveExternalTable(list,null,"Oracle", "Hive");
        logger.info(String.format("Build sql statement elapsed time: %d", System.currentTimeMillis() - start));
        logger.info(sql);
        return sql;
    }

}