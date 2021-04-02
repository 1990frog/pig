package com.clinbrain.dip.rest.vo;

import lombok.Data;

import javax.persistence.Column;
import java.util.List;

/**
 * Created by Liaopan on 2020-11-25.
 */
@Data
public class ModuleWorkflowStatus {

	private String moduleCode;
	private String uuid;
	private Integer status;

	List<WorkflowStatus> workflowStatusList;
}
