package com.clinbrain.dip.strategy.util;

import com.clinbrain.dip.strategy.sqlparse.FromTableItem;
import com.clinbrain.dip.strategy.sqlparse.MySelectVisitor;
import com.clinbrain.dip.strategy.sqlparse.MyTableFromVisitor;
import com.clinbrain.dip.strategy.sqlparse.TableColumnItem;
import net.sf.jsqlparser3.JSQLParserException;
import net.sf.jsqlparser3.parser.CCJSqlParserUtil;
import net.sf.jsqlparser3.statement.Statement;
import net.sf.jsqlparser3.statement.select.Select;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Liaopan on 2020-10-13.
 * 处理 sql 中的select 列 和 from 表的对应
 */
public class CCJSqlParseUtil {

	public static Set<FromTableItem> getTableColumns(String sql) throws JSQLParserException {
		final Statement sta = CCJSqlParserUtil.parse(sql);
		if (sta instanceof Select) {
			Select selectStatement = (Select) sta;
			MyTableFromVisitor tablesNamesFinder = new MyTableFromVisitor();
			final Set<FromTableItem> tableSet = tablesNamesFinder.getTableSet(selectStatement);
			return tableSet;
		}
		return new HashSet<>();
	}

}

