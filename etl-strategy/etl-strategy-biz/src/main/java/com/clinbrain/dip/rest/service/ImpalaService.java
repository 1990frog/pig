package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.rest.request.SQLRequest;
import com.clinbrain.dip.rest.response.SQLResponse;
import com.clinbrain.dip.util.DBUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.UUID;

@Component("impalaService")
public class ImpalaService {
    private static final Logger logger = LoggerFactory.getLogger(ImpalaService.class);

    @Value("${impala.url}")
    private String url;
    @Value("${impala.username}")
    private String username;
    @Value("${impala.password}")
    private String password;

    public SQLResponse query(SQLRequest sqlRequest) {
        IDatabaseClient impala = DatabaseClientFactory.getDatabaseClient(url, username, password);
        SQLResponse sqlResponse = new SQLResponse();
        String uuid = UUID.randomUUID().toString();
        List<List<String>> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            String sql = sqlRequest.getMdxDescData();
            long startTime = System.currentTimeMillis();
            rs = impala.queryWithResultSet(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columns = Lists.newArrayListWithCapacity(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnLabel(i));
            }
            while (rs.next()) {
                List<String> oneRow = Lists.newArrayListWithCapacity(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    oneRow.add(rs.getString(i));
                }
                results.add(oneRow);
            }
            sqlResponse = new SQLResponse(columns, results);
            sqlResponse.setDuration(System.currentTimeMillis() - startTime);
            sqlResponse.setTotalScanCount(results.size());

            logger.info("Impala sql " + uuid + ": execute successfully!");
        } catch (Exception e) {
            logger.error("Impala sql " + uuid + ": execute failed!\n" + e.getMessage());
        } finally {
            DBUtils.closeQuietly(rs);
            impala.close();
        }
        return sqlResponse;
    }
}
