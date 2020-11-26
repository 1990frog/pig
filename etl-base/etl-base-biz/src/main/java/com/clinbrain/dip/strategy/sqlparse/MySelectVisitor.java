package com.clinbrain.dip.strategy.sqlparse;

import net.sf.jsqlparser3.statement.select.SelectExpressionItem;
import net.sf.jsqlparser3.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser3.util.TablesNamesFinder;

import java.util.Set;

/**
 * Created by Liaopan on 2020-10-13.
 */
public class MySelectVisitor extends TablesNamesFinder {

	protected final Set<TableColumnItem> selectColumnItemList;

	public MySelectVisitor(Set<TableColumnItem> columnItems) {
		this.selectColumnItemList = columnItems;
	}


	public static class MySelectItemVisitor extends SelectItemVisitorAdapter {

		private final Set<TableColumnItem> columnItems;

		public MySelectItemVisitor(Set<TableColumnItem> columnItems) {
			this.columnItems = columnItems;
		}

		@Override
		public void visit(SelectExpressionItem selectExpressionItem) {
			selectExpressionItem.getExpression().accept(new MyExpressionVisitor(columnItems));
		}
	}
}
