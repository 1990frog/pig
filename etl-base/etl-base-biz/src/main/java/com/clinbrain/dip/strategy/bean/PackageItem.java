package com.clinbrain.dip.strategy.bean;

import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import lombok.Data;

import java.util.Map;

/**
 * Created by Liaopan on 2020-10-17.
 * 代表策略包中的每一项内容，包含一个任务和对应的sql语句
 */
@Data
public class PackageItem {

	private String moduleCode;
	private ModuleTaskRequest moduleInfo;

	private Map<String,String> workflowSqlMap;
}
