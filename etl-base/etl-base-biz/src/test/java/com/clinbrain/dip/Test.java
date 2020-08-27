package com.clinbrain.dip;

import cn.hutool.core.lang.Console;
import cn.hutool.http.webservice.SoapClient;
import cn.hutool.http.webservice.SoapProtocol;
import com.clinbrain.dip.metadata.Sql;
import com.sun.xml.internal.messaging.saaj.soap.name.NameImpl;
import lombok.SneakyThrows;
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

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
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

	@org.junit.Test
	@SneakyThrows
	public void testWebService() {
		final SoapClient client = SoapClient.create("http://192.168.0.35:9011/cws/UserWebService.asmx", SoapProtocol.SOAP_1_2);
		final SOAPHeader soapHeader = client.getMessage().getSOAPHeader();
			soapHeader.addAttribute(new QName("CurrentOperateUserCode"),"sys");
			soapHeader.addAttribute(new QName("CurrentAppName"),"5aSn5pWw5o2u566h55CG5bmz5Y+w");
			soapHeader.addAttribute(new QName("Sign"),"asdf");
			soapHeader.addAttribute(new QName("TimeStamp"),""+System.currentTimeMillis()/1000);
		client
			.setMethod("GetUser", "http://Centralism.WebService/")
			.setParam("userCode","sys");
		Console.log(client.sendForMessage().getSOAPHeader());
		System.out.println(client.send(true));
	}
}
