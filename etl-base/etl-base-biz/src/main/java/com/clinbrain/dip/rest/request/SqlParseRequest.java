package com.clinbrain.dip.rest.request;

import com.clinbrain.dip.sqlparse.ParseSql;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Liaopan on 2018/10/30.
 */
@Data
public class SqlParseRequest implements Serializable {

    private String incrementalWhere;
    private String rangeWhere;
    private String sql;
    private ParseSql.SqlType sqlType;
}
