package com.clinbrain.dip;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.db.Db;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.metadata.Sql;
import com.clinbrain.dip.sqlparse.ParseSql;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.sqlparse.FromTableItem;
import com.clinbrain.dip.strategy.sqlparse.MyTableFromVisitor;
import com.clinbrain.dip.util.SqlParseUtil;
import com.clinbrain.dip.workflow.ETLStart;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.sf.jsqlparser3.parser.CCJSqlParserUtil;
import net.sf.jsqlparser3.statement.Statement;
import net.sf.jsqlparser3.statement.select.Select;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;

/**
 * Created by Liaopan on 2020/7/30 0030.
 */

public class Test {

	@org.junit.Test
	public void testSqlParse() throws Exception {
		BufferedReader fileReader
			= new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\User\\Desktop\\temp\\new5.sql")));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = fileReader.readLine()) != null) {
			sb.append(StringUtils.substringBefore(line,"--")).append("  ");
		}
		System.out.println(sb);
		final Statement sta = CCJSqlParserUtil.parse(sb.toString());
		if (sta instanceof Select) {
			Select selectStatement = (Select) sta;
			MyTableFromVisitor tablesNamesFinder = new MyTableFromVisitor();
			final Set<FromTableItem> tableSet = tablesNamesFinder.getTableSet(selectStatement);
		}
		//SqlParseUtil.parseSql(sb.toString(), ParseSql.SqlType.ORACLE);
	}

	@org.junit.Test
	public void testZipFile() {

		final PackageInfo packageInfo = new PackageInfo("费用相关", "v1.0", "HIS");
		final TemplateConfig templateConfig = new TemplateConfig();
		templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
		TemplateEngine engine = TemplateUtil.createEngine(templateConfig);

		Template template = engine.getTemplate("/beetl/system.json.tmpl");

		final String render = template.render(BeanUtil.beanToMap(packageInfo));

		String zippath = "E:/data/abc.clb";
		/** 一次性压缩多个文件，文件存放至一个文件夹中*/
		try {
			ZipFile zipFile = new ZipFile(zippath);

			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(EncryptionMethod.AES);
			zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
			zipParameters.setFileNameInZip("system.json");

			zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());

			zipFile.addStream(new ByteArrayInputStream(render.getBytes(StandardCharsets.UTF_8)), zipParameters);

			for(int i =0;i < 10; i++) {
				zipParameters.setFileNameInZip("i_" +i +".json");
				zipFile.addStream(new ByteArrayInputStream(("第"+i+"个文件").getBytes()), zipParameters);
			}

			System.out.println(zipFile.getFile().length());
			// 添加签名
			final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);
			final FileInputStream fileInputStream = new FileInputStream(zipFile.getFile());
			byte[] cc = new byte[fileInputStream.available()];
			fileInputStream.read(cc);
			byte[] signed = sign.sign(cc);
			System.out.println("signed(长度):" + signed.length);
			ZipParameters ccParams = new ZipParameters();
			ccParams.setFileNameInZip("cc");
			zipFile.addStream(new ByteArrayInputStream(signed), ccParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@org.junit.Test
	public void testUnzipFile() throws IOException {
		ZipFile zipFile = new ZipFile("E:/data/abc.clb");

		if(zipFile.isEncrypted()) {
			zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());
		}
		System.out.println("验证zip文件： " + zipFile.isValidZipFile());
		final FileHeader ccFile = zipFile.getFileHeader("cc");
		InputStream is = zipFile.getInputStream(ccFile);
		final byte[] signed = IOUtils.toByteArray(is);
		System.out.println("is大小：" + is.available());
		System.out.println("signed大小：" + signed.length);

		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

		ZipFile newFile = new ZipFile(zipFile.getFile().getName(), PACKAGE_PASSWORD.toCharArray());
		zipFile.getFileHeaders().stream()
			.filter(f -> !"cc".equalsIgnoreCase(f.getFileName())).forEach(f -> {
				try {
					zipParameters.setFileNameInZip(f.getFileName());
					newFile.addStream(zipFile.getInputStream(f), zipParameters);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		// 获取验证
		final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);
		final FileInputStream fileInputStream = new FileInputStream(newFile.getFile());
		byte[] fileContent = new byte[fileInputStream.available()];
		fileInputStream.read(fileContent);

		final boolean verify = sign.verify(fileContent, signed);

		System.out.println("验证通过？ " + verify);
	}

	@org.junit.Test
	public void testSign() throws IOException {
		final Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withECDSA);

		FileInputStream reader = new FileInputStream(new File("E:\\data\\aa.clb"));
		final int size = reader.available();
		System.out.println("总大小：" + size);
		byte[] content = new byte[size];
		reader.read(content);
		System.out.println("读取大小：" + content.length);
		byte[] signed = sign.sign(content);

		System.out.println(sign.verify(content, signed));
		System.out.println(sign.getPrivateKey().toString());
		System.out.println(sign.getPublicKey().toString());
	}

	@org.junit.Test
	public void testEtlStart() throws Exception{
		ETLStart.startByModule("ETL_FACT_regex_test_SZSL-001_1610097246982", UUID.randomUUID().toString());
	}

	@org.junit.Test
	public void testDBQuery() throws Exception{
		String sql = "select * from dbo.cda_main a ";
		String jdbcUrl = "jdbc:sqlserver://192.168.0.114:1433;DatabaseName=binzhou_HLHT";
		//String jdbcUrl = "jdbc:mysql://192.168.0.112:3306/dataconfig";
		String user = "sa";
		String password = "cdr123!@#";
		final DbType dbType = JdbcUtils.getDbType(jdbcUrl);
		IDialect dialect = DialectFactory.getDialect(dbType);
		String buildSql = sql;
		DialectModel model = dialect.buildPaginationSql(buildSql, 0, 10);

		final IDatabaseClient sa = DatabaseClientFactory.getDatabaseClient(jdbcUrl, user, password);
		ResultSet resultSet = null;
		try {
			System.out.println(model.getDialectSql());
			resultSet = sa.queryWithResultSet(model.getDialectSql());

			while (resultSet.next()) {
				System.out.println(resultSet.getString(1));
			}
		}finally {
			System.out.println("准备关闭");
			resultSet.close();
			System.out.println("关闭sa");
			sa.close();
		}



		/*QueryRunner queryRunner = new QueryRunner(sa.getDataSource());
		List<Map<String,Object>> dataList = new ArrayList<>(10);
		queryRunner.execute(sql, (ResultSetHandler<Object>) rs -> {
			int i = 1;
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();
			Map<String, Object> dataMap;
			while (rs.next()) {
				if(i <= 10) {
					dataMap = new HashMap<>();
					for(int j = 1; j <= columnCount; j++) {
						dataMap.put(metaData.getColumnName(j), rs.getString(j));
					}
					dataList.add(dataMap);
					i++;
				}else {
					break;
				}
			}
			return null;
		}, null);

		System.out.println(dataList);*/
	}

	@org.junit.Test
	public void testAes() {
		String pass = "thanks,pig4cloud";
		String data = "LFEt60GoMTj5/mQQ7HjJeA==";
		AES aes = new AES(Mode.CBC, Padding.NoPadding, new SecretKeySpec(pass.getBytes(), "AES"),
				new IvParameterSpec(pass.getBytes()));
		byte[] result = aes.decrypt(Base64.decode(data.getBytes(StandardCharsets.UTF_8)));
		System.out.println(new String(result, StandardCharsets.UTF_8));
	}

}
