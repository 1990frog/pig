package com.clinbrain.dip.strategy.util;

import cn.hutool.core.io.FileUtil;
import com.clinbrain.dip.strategy.bean.PackageInfo;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.clinbrain.dip.strategy.constant.TacticsConstant.PACKAGE_NAME_SUFFIX;

/**
 * Created by Liaopan on 2020/8/14.
 */
public class PackageUtil {

	/**
	 * 获取目录下的策略包文件
	 * @param path
	 * @return
	 */
	public static List<PackageInfo> fetchPackageInfos(String path) {
		final File[] files = FileUtil.ls(path);
		for (File file : files) {
			file.listFiles((dir, name) -> {
				if(name.endsWith(PACKAGE_NAME_SUFFIX)) {
					return true;
				}
				return false;
			});
		}
		return Stream.of(files).map(f -> new PackageInfo(f.getName(),"", null))
			.collect(Collectors.toList());
	}
}
