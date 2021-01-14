package com.clinbrain.dip.strategy.service;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.rest.service.ConnectionService;
import com.clinbrain.dip.strategy.util.MyJdbcUtils;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2021-01-13.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SqlQueryService {

	private final ConnectionService connectionService;

	private int DEFAULT_COUNT = 10;

	/**
	 * 提供根据连接code,sql 执行sql查询的功能
	 * @param connectionCode 连接code, etl_connection表中的code
	 * @param sql 要执行的sql
	 * @return 数据结果集（List<Map<String,Object>>）
	 * @throws Exception
	 */
	public List<Map<String,Object>> queryList(String connectionCode, String sql) throws Exception {
		final ETLConnection etlConnection = connectionService.selectOne(connectionCode);
		Preconditions.checkNotNull(etlConnection, "找不到数据源配置" + connectionCode);
		return queryList(etlConnection.getUrl(), etlConnection.getUser(), etlConnection.getPassword(), sql);
	}

	/**
	 * 提供连接串和sql执行查询
	 * @param jdbcUrl
	 * @param userName
	 * @param password
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryList(String jdbcUrl, String userName, String password, String sql) throws Exception{
		final IDatabaseClient sa = DatabaseClientFactory
			.getDatabaseClient(jdbcUrl, userName, password);
		List<Map<String,Object>> dataList = new ArrayList<>(DEFAULT_COUNT);
		final DbType dbType = MyJdbcUtils.getDbType(jdbcUrl);
		IDialect dialect = DialectFactory.getDialect(dbType);
		DialectModel model = dialect.buildPaginationSql(sql, 0, DEFAULT_COUNT);
		String s = String.format(model.getDialectSql().replaceFirst("\\?"," %d"), DEFAULT_COUNT); // 这里的sql 含有? 需要替换掉
		try (ResultSet rs = sa.queryWithResultSet(s)){
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();
			Map<String, Object> dataMap;
			while (rs.next()) {
				dataMap = new HashMap<>();
				for(int j = 1; j <= columnCount; j++) {
					dataMap.put(metaData.getColumnName(j), rs.getString(j));
				}
				dataList.add(dataMap);
			}
		}finally {
			sa.close();
		}
		return dataList;
	}
}
