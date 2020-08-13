package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.ConnectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/etl/connection")
public class ConnectionController {

    @Autowired
    ConnectionService dipConnectionService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseData getAll() {
        List<ETLConnection> dipEtlConnections = dipConnectionService.selectAll();
        return new ResponseData.Builder<List>(dipEtlConnections).success();
    }

    /**
     * 获取数据库库
     * @param connectionCode 连接编码
     * @param dbName 库名
     * @param tableName 表名
     * @return
     */
    @GetMapping("/database")
    public ResponseData getDatabases(@RequestParam String connectionCode,
                                     @RequestParam(required = false) String dbName,
                                     @RequestParam(required = false) String tableName) {
        if (StringUtils.isNotEmpty(connectionCode)) {
            return new ResponseData.Builder<List>(dipConnectionService.getDataBases(connectionCode, dbName, tableName)).success();
        }
        return new ResponseData.Builder<>().error("");
    }


    @GetMapping("/columns")
    public ResponseData showTableColumnsByDBName(@RequestParam String connectionCode,
                                                 @RequestParam(required = false) String dbName) {
        if (StringUtils.isNotEmpty(connectionCode)) {
            return new ResponseData.Builder<>(dipConnectionService.getDataBases(connectionCode, dbName)).success();
        }
        return new ResponseData.Builder<>().error("");
    }

    @DeleteMapping(value = "/{code:.+}")
    public ResponseData DeleteComponent(@PathVariable("code") String code) {
        if (dipConnectionService.checkConnectionByModule(code) > 0) {
            return new ResponseData.Builder<>(null).error("连接已经被使用，无法删除");
        }
        ETLConnection connection = new ETLConnection();
        connection.setConnectionCode(code);
        return new ResponseData.Builder<>(dipConnectionService.deleteByPrimaryKey(connection)).success();
    }

    @PutMapping
    public ResponseData renovateComponent(@RequestBody ETLConnection connection) {
        if (connection == null) {
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipConnectionService.putConnection(connection)).success();
    }

    @PostMapping
    public ResponseData appendComponent(@RequestBody ETLConnection connection) {
        if (connection == null) {
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipConnectionService.appendConnection(connection)).success();
    }

    /**
     * 验证sql语句语法
     */
    @GetMapping("/{code}/validsql")
    public ResponseData validateSql(@RequestParam("sql") String sql, @PathVariable("code") String connectionCode) {
        String errMsg = dipConnectionService.validSql(connectionCode, sql);
        if (StringUtils.isEmpty(errMsg)) {
            return new ResponseData.Builder<>().success();
        } else {
            return new ResponseData.Builder<>().error(errMsg);
        }
    }

    /**
     * 校检连接是否成功
     *
     * @param connection 连接对象
     * @return
     */
    @PostMapping("/verify")
    public ResponseData verifyConnect(@RequestBody ETLConnection connection) {
        try {
            int connectCount = dipConnectionService.getDateBasesCount(connection);
            if (connectCount > 0) {
                return new ResponseData.Builder<>(connectCount).success("连接成功");
            } else {
                return new ResponseData.Builder<>(connectCount).error("连接失败");
            }
        } catch (Exception e) {
            return new ResponseData.Builder<>().error("连接失败");
        }
    }

    @GetMapping("/clear")
    @CacheEvict(value = {"hiveTables","hiveTableColumns","databaseTableColumns"}, allEntries = true)
    public ResponseData clearCache() {
        return new ResponseData.Builder<>("成功清除 hiveTables，hiveTableColumns，databaseTableColumns缓存")
                .success("操作成功");

    }
}
