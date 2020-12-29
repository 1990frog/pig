package com.clinbrain.dip.rest.vo;

import lombok.Data;

import java.util.List;

/**
 * Created by Liaopan on 2020-11-25.
 */
@Data
public class ModuleWorkflowStatus {

	private String moduleCode;
	private Integer status;

	List<WorkflowStatus> workflowStatusList;
}
