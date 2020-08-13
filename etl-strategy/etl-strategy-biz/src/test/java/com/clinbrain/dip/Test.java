package com.clinbrain.dip;

import com.clinbrain.dip.metadata.Sql;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.NotImplementedException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Liaopan on 2020/7/30 0030.
 */

public class Test {

	@org.junit.Test
	public void test1() throws Exception {
		BufferedReader fileReader
			= new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\User\\Desktop\\sql脚本.sql"), Charset.forName("GB2312")));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = fileReader.readLine()) != null) {
			sb.append(line).append(" ");
		}

	}
}
