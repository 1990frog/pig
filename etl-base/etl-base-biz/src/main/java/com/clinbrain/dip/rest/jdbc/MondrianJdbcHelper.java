package com.clinbrain.dip.rest.jdbc;

import com.clinbrain.dip.metadata.ConnectionPoolProperty;
import com.clinbrain.dip.rest.response.SQLResponse;
import com.google.common.collect.Lists;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import java.sql.Connection;
import java.util.List;

public class MondrianJdbcHelper {
    private ConnectionPoolProperty properties;

    public MondrianJdbcHelper(ConnectionPoolProperty properties) {
        this.properties = properties;
    }

    public SQLResponse executeMDXQuery(String mdx) throws Exception {
        SQLResponse response;
        OlapConnection cnct = null;
        OlapStatement stmt = null;
        CellSet cs = null;
        try {
            long startTime = System.currentTimeMillis();
            Connection connection  = JdbcConnectionManager.getMondrianJdbcDataSource(properties).getConnection();
            cnct = connection.unwrap(OlapConnection.class);
            stmt = cnct.createStatement();
            cs = stmt.executeOlapQuery(mdx);
            response = getOlapData(cs);
            response.setDuration(System.currentTimeMillis() - startTime);
        } finally {
            if (cs != null) {
                cs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (cnct != null) {
                cnct.close();
            }
        }
        return response;
    }

    public SQLResponse getOlapData(CellSet cs) {
        SQLResponse response = new SQLResponse();
        List<String> header = Lists.newArrayList();
        List<List<String>> result = Lists.newArrayList();
        List<String> oneRow;
        boolean headerFlag = true;

        if (cs.getAxes().size() == 2) {
            for (Position row : cs.getAxes().get(1)) { // Row轴
                oneRow = Lists.newArrayList();

                for (Member member : row.getMembers()) {
                    if (headerFlag) { // 获取Row轴上的列名
                        header.add(member.getDimension().getCaption());
                    }
                    oneRow.add(member.getName());
                }

                for (Position column : cs.getAxes().get(0)) {
                    if (headerFlag) { // 获取Column轴上的列名
                        header.add(column.getMembers().get(0).getName()); //一维列头
                    }
                    oneRow.add(cs.getCell(column, row).getFormattedValue());
                }
                headerFlag = false;
                result.add(oneRow);
            }
        } else if (cs.getAxes().size() == 1) { // 只有Column轴
            for (Position column : cs.getAxes().get(0)) {
                oneRow = Lists.newArrayList();
                header.add(column.getMembers().get(0).getName());
                oneRow.add(cs.getCell(column).getFormattedValue());
                result.add(oneRow);
            }
        }
        response.setColumnName(header);
        response.setResults(result);
        response.setTotalScanCount(result.size());
        return response;
    }

}
