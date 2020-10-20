package com.clinbrain.dip.strategy.sqlparse;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2020-10-16.
 */
public class MyTableFromVisitor extends TablesNamesFinder {

	private Set<FromTableItem> tableItems;

	private Set<TableColumnItem> tableColumnItemSet;


	public Set<FromTableItem> getTableSet(Statement statement) {
		init();
		statement.accept(this);
		tableItems.forEach(table -> {
			table.setColumnItems(tableColumnItemSet.stream()
				.filter(c -> StringUtils.equalsIgnoreCase(c.getTableAliasName(),table.getTableAliasName()))
				.collect(Collectors.toList()));
		});
		return tableItems;
	}

	@Override
	protected void init() {
		super.init();
		this.tableItems = new HashSet<>();
		this.tableColumnItemSet = new HashSet<>();
	}

	@Override
	public void visit(Table tableName) {
		tableItems.add(new FromTableItem(Optional.ofNullable(tableName.getDatabase())
			.map(Database::getDatabaseName).orElse(tableName.getSchemaName())
			,tableName.getSchemaName()
			,tableName.getName()
			,Optional.ofNullable(tableName.getAlias()).map(Alias::getName).orElse("")));
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		if (plainSelect.getSelectItems() != null) {
			for (SelectItem item : plainSelect.getSelectItems()) {
				item.accept(this);
			}
		}

		if (plainSelect.getFromItem() != null) {
			plainSelect.getFromItem().accept(this);
		}

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(this);
				join.getOnExpression().accept(this);
			}
		}
		if (plainSelect.getWhere() != null) {
			plainSelect.getWhere().accept(this);
		}
		if (plainSelect.getOracleHierarchical() != null) {
			plainSelect.getOracleHierarchical().accept(this);
		}
	}

	@Override
	public void visit(Column column) {
		TableColumnItem columnItem = new TableColumnItem();
		final Table table = column.getTable();
		columnItem.setTableAliasName(Optional.ofNullable(table).map(Table::getName).orElse(""));
		columnItem.setColumnName(column.getColumnName());
		tableColumnItemSet.add(columnItem);
	}

	@Override
	public void visit(WhenClause whenClause) {
		if(whenClause.getWhenExpression() != null) {
			whenClause.getWhenExpression().accept(this);
		}

		if(whenClause.getThenExpression() != null) {
			whenClause.getThenExpression().accept(this);
		}
	}

	@Override
	public void visit(CaseExpression expr) {
		if(expr.getSwitchExpression() != null) {
			expr.getSwitchExpression().accept(this);
		}
		for (Expression x : expr.getWhenClauses()) {
			x.accept(this);
		}
		if(expr.getElseExpression() != null) {
			expr.getElseExpression().accept(this);
		}
	}

}
