package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.metadata.Sql;
import com.clinbrain.dip.rest.bean.PropertyBean;
import com.clinbrain.dip.rest.request.SqlParseRequest;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.util.SqlParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class IndexController {
    @Value("${spring.datasource.druid.url}")
    private String url;
    @Value("${spring.datasource.druid.driver-class-name}")
    private String driverClass;
    @Value("${spring.datasource.druid.username}")
    private String username;
    @Value("${spring.datasource.druid.password}")
    private String password;

    @Value("${azkabanJobManage.serverAddress}")
    private String serverAddress;

    @Autowired
    private PropertyBean property;

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(value = "/test" ,method = RequestMethod.GET)
    @ResponseBody
    public ResponseData index() {
        Map<String, String> dbProperty = new HashMap<>();
        System.out.println(url);
        System.out.println(driverClass);
        System.out.println(username + ":" + password);
        dbProperty.put("url", url);
        dbProperty.put("driver", driverClass);
        dbProperty.put("user", null);
        dbProperty.put("pwd", null);
        Map dataMap = new LinkedHashMap();
        Boolean isValid;
        try {
            //isValid = DBUtils.testConnection(url, driverClass, username, password);
            IDatabaseClient databaseClient = DatabaseClientFactory.getDatabaseClient(url, username, password);
            isValid = databaseClient.getConnection().isValid(1000);
            List<String> dbList = databaseClient.getDbNames();
            dataMap.put("数据库连接状态", isValid);
            dataMap.put("数据库信息", dbProperty);
            dataMap.put("数据库列表", dbList);
            dataMap.put("kylin连接信息", property.getKylinHostname());
            dataMap.put("azkaban", serverAddress);
        } catch (Exception e) {
            return new ResponseData.Builder<Map>(dataMap).status(ResponseData.Status.ERROR).message("系统出错！").build();
        }
        return new ResponseData.Builder<Map>(dataMap).status(ResponseData.Status.SUCCESS).message("系统相关配置信息.").build();

    }

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData kylinProperty() {
        logger.info(property.getKylinUsername());
        logger.info(property.getKylinPassword());
        return new ResponseData.Builder<String>(property.getKylinHostname())
                .status(ResponseData.Status.SUCCESS).message("查询kylin配置信息成功！").build();
    }

    /**
     * sql 解析
     * @param request SqlParseRequest
     * @return
     */
    @PostMapping("/parsesql")
    @ResponseBody
    public ResponseData parseSql(@RequestBody SqlParseRequest request){
        try {
            Sql obj = SqlParseUtil.parseSql(request.getSql(), request.getSqlType());
            return new ResponseData.Builder<>(obj).success();
        } catch (Exception e) {
            logger.error("解析sql出错",e);
            return new ResponseData.Builder<>().error(e.getMessage());
        }
    }

    /**
     * 系统日志
     * @param fileName
     * @return
     */
    @GetMapping("/logs")
    @ResponseBody
    public ResponseData showLog(@RequestParam(value = "fileName",required = false) String fileName) {

		return new ResponseData.Builder<>().data("").success();
    }

}
