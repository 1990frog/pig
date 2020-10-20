package com.clinbrain.dip.strategy.sqlparse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liaopan on 2020-10-16.
 */
@Data
@NoArgsConstructor
public class FromTableItem {

	private String databaseName;
	private String tableSchema;
	private String tableName;
	private String tableAliasName;

	public FromTableItem(String databaseName, String tableSchema, String tableName, String tableAliasName) {
		this.databaseName = databaseName;
		this.tableSchema = tableSchema;
		this.tableName = tableName;
		this.tableAliasName = tableAliasName;
	}

	private List<TableColumnItem> columnItems = new ArrayList<>();
}
