package com.clinbrain.dip.strategy.sqlparse;

import net.sf.jsqlparser3.expression.CaseExpression;
import net.sf.jsqlparser3.expression.Expression;
import net.sf.jsqlparser3.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser3.expression.UserVariable;
import net.sf.jsqlparser3.schema.Column;
import net.sf.jsqlparser3.schema.Database;
import net.sf.jsqlparser3.schema.Table;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Liaopan on 2020-10-13.
 */
public class MyExpressionVisitor extends ExpressionVisitorAdapter {

	private final Set<TableColumnItem> columnItemSet;

	public MyExpressionVisitor(Set<TableColumnItem> columnItemList) {
		this.columnItemSet = columnItemList;
	}

	@Override
	public void visit(Column column) {
		TableColumnItem columnItem = new TableColumnItem();
		final Table table = column.getTable();
		columnItem.setTableAliasName(Optional.ofNullable(table).map(Table::getName).orElse(""));
		columnItem.setColumnName(column.getColumnName());
		this.columnItemSet.add(columnItem);
	}

	@Override
	public void visit(CaseExpression expr) {
		if(expr.getSwitchExpression() != null) {
			expr.getSwitchExpression().accept(this);
		}
		for (Expression x : expr.getWhenClauses()) {
			x.accept(this);
		}
		expr.getElseExpression().accept(this);
	}

	@Override
	public void visit(UserVariable var) {
		super.visit(var);
	}

}
