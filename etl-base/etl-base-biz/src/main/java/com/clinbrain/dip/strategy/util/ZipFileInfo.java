package com.clinbrain.dip.strategy.util;

import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.google.gson.Gson;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.IOUtils;
import parquet.Preconditions;

import java.io.InputStream;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_PASSWORD;
import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_SYSTEM_INFO;

/**
 * Created by Liaopan on 2020-09-05.
 */
public class ZipFileInfo {

	private static Gson gson = new Gson();

	public static PackageInfo readZipSystemInfo(ZipFile zipFile) throws Exception {

		InputStream is = null;
		try {
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(PACKAGE_PASSWORD.toCharArray());
			}

			final FileHeader systemFile = zipFile.getFileHeader(PACKAGE_SYSTEM_INFO);
			Preconditions.checkNotNull(systemFile, "不是有效的策略包文件! ");
			is = zipFile.getInputStream(systemFile);
			final byte[] bytes = IOUtils.toByteArray(is);
			return gson.fromJson(new String(bytes), PackageInfo.class);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
