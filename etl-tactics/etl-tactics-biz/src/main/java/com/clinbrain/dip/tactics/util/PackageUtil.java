package com.clinbrain.dip.tactics.util;

import cn.hutool.core.io.FileUtil;
import com.clinbrain.dip.tactics.bean.PackageInfo;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Liaopan on 2020/8/14 0014.
 */
public class PackageUtil {

	public static List<PackageInfo> fetchPackageInfos(String path) {
		final File[] files = FileUtil.ls(path);
		for (File file : files) {
			System.out.println(file.getName());
		}
		return Stream.of(files).map(f -> new PackageInfo(f.getName(),"",""))
			.collect(Collectors.toList());
	}
}
