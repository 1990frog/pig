package com.clinbrain.dip.rest.request;

import com.clinbrain.dip.jackson.DefaultDateNullValueDeserializer;
import com.clinbrain.dip.metadata.WorkflowExtraData;
import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLWorkflowSelectRegex;
import com.clinbrain.dip.pojo.EtlCommonExpression;
import com.clinbrain.dip.pojo.EtlJobModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Liaopan on 2018/3/12.
 */
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ModuleTaskRequest{

    private String moduleCode;
    // 主要用来生成moduleCode
    private String moduleName;
    private String moduleDescription;
    private String connectionCode;
    private String targetSchema;
    private String targetTable;
    private Integer modulePriority;
    private String moduleCategory;
    private Integer etlType = 1; // 0: incremental, 1: full, 2: range
    private Integer enabled = 0;
    private Integer fullWhileMonths;
    private String dependencyCode;
    private Long rangeStartDate;
    private Long rangeEndDate;
    private Integer topicId;
    private Integer engineId;
    private Integer jobId;
	private ETLJob etlJob;
    private String hospitalName;
    @JsonDeserialize(using = DefaultDateNullValueDeserializer.class)
    private Date createdAt;
    @JsonDeserialize(using = DefaultDateNullValueDeserializer.class)
    private Date updatedAt;

    private String numberRange;

    private List<InnerWorkflow> workflows;


    @Data
    public static class InnerWorkflow{

		@JsonProperty("workflowCode")
    	private String workflowCodeOrigin;
        @JsonProperty("key")
        private String workflowCode;
        @JsonProperty("name")
        private String workflowName;
        @JsonProperty("code")
        private String workflowSequenceDefault;
        @JsonProperty("sequenceCustomized")
        private Integer sequenceCustomized;
        @JsonProperty("paramDefine")
        private String paramDefine;

        private String loc;

        private Integer runnable;

        private String incrementalMode = "union";

        @JsonProperty("componentCategory")
        private String category;

        @JsonProperty("category") // Group /  GroupNode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String groupCategory;

        private String color;

        private String targetSchema;
        private String targetTable;
        @JsonProperty("parameterJson")
        private WorkflowParamInfo parameterJson;
        @JsonProperty("desc")
        private String desc;

        private WorkflowExtraData extraData;
        @JsonProperty("flowType")
        private Integer dataFlowType;

        private String workflowSql;

		/**
		 * 2021-01-11 新增加字段列的正则表达式匹配
		 */
		@JsonProperty("columnRegex")
		private List<ETLWorkflowSelectRegex> selectColumnRegex;

    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkflowParamInfo extends HashMap {

        private String sourceSchema;

        private String sourceTable;

        private String sourceIncrementalColumn;

        private String targetSchema;

        private String targetTable;

        private String targetIncrementalColumn;

        private String targetPartitionColumn;

        private String extractTable;

        private String partitionTable;

        private String sourceTableUniqueKey;

        //liaopan 2019-11-16 增加merge 组件参数定义
        // 合并方式
        private String mergeWay;

        // 合并选择的列
        private List<String> newColumns;

    }
}
