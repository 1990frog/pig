package com.clinbrain.dip.rest.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.clinbrain.dip.common.DipConfig;
import com.clinbrain.dip.connection.DatabaseClientFactory;
import com.clinbrain.dip.connection.IDatabaseClient;
import com.clinbrain.dip.metadata.CommonConstant;
import com.clinbrain.dip.metadata.WorkflowExtraData;
import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.pojo.ETLHospital;
import com.clinbrain.dip.pojo.ETLLogSummary;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLWorkflow;
import com.clinbrain.dip.pojo.ETLWorkflowConnection;
import com.clinbrain.dip.pojo.ETLWorkflowDataxflow;
import com.clinbrain.dip.pojo.ETLWorkflowToken;
import com.clinbrain.dip.pojo.ETLWorkflowTokenFilter;
import com.clinbrain.dip.pojo.ETLWorkflowTokenFromOrJoin;
import com.clinbrain.dip.pojo.ETLWorkflowTokenFullSql;
import com.clinbrain.dip.pojo.EtlJobModule;
import com.clinbrain.dip.rest.mapper.DBETLJobModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLLogSummaryMapper;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowConnectionMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowDataxflowMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowTokenFilterMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowTokenFromOrJoinMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowTokenFullSqlMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowTokenMapper;
import com.clinbrain.dip.rest.mapper.DBETLWorkflowTokenSelectMapper;
import com.clinbrain.dip.rest.request.ModuleTaskRequest;
import com.clinbrain.dip.strategy.entity.JobVersion;
import com.clinbrain.dip.strategy.mapper.VersionMapper;
import com.clinbrain.dip.workflow.ETLStart;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.Weekend;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class ModuleService extends BaseService<ETLModule> {

	@Autowired
	private DBETLModuleMapper moduleMapper;

	@Autowired
	private DBETLWorkflowMapper workflowMapper;

	@Autowired
	private DBETLWorkflowConnectionMapper workflowConnectionMapper;

	@Autowired
	private DBETLWorkflowTokenMapper workflowTokenMapper;

	@Autowired
	private DBETLWorkflowTokenSelectMapper selectMapper;

	@Autowired
	private DBETLWorkflowTokenFromOrJoinMapper fromOrJoinMapper;

	@Autowired
	private DBETLWorkflowTokenFilterMapper filterMapper;

	@Autowired
	private DBETLWorkflowTokenFullSqlMapper fullSqlMapper;

	@Autowired
	private DBETLWorkflowDataxflowMapper workflowDataxflowMapper;


    private VersionMapper versionMapper;

    @Autowired
    @Qualifier("jobModuleMapper")
    private DBETLJobModuleMapper jobModuleMapper;

	@Autowired
	private ConnectionService connectionService;

	@Value("${check.url}")
	private String checkDataUrl;

	@Autowired
	private DBETLLogSummaryMapper logSummaryMapper;

	public ETLModule selectModuleDetailByCode(String moduleCode) {
		if (StringUtils.isNotEmpty(moduleCode)) {
			return moduleMapper.selectModuleDetailByCode(moduleCode);
		}
		return null;
	}

	public List<ETLModule> queryModuleDetails(Integer topicId, Integer jobId, String hospital, String moduleName) {
		try {
			List<ETLModule> list = moduleMapper.selectModuleDetails(topicId, jobId, hospital, moduleName);
			if (list.size() > 0) {
				return list;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<ETLModule> queryAllModules(Integer topicId, Integer jobId, String hospital, String moduleName) {
		try {
			List<ETLModule> list = moduleMapper.selectAllModules(topicId, jobId, hospital, moduleName);
			if (list.size() > 0) {
				return list;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModuleTaskRequest selectModuleDetail(String code) {
		ETLModule module = selectModuleDetailByCode(code);
		if (module == null) {
			return null;
		}


        /*try {
            if (StringUtils.isNotEmpty(module.getModuleJsonInfo())) {
                ObjectMapper objectMapper = new ObjectMapper();
                ModuleTaskRequest.ModuleExtraDataInfo info = objectMapper.readValue(module.getModuleJsonInfo(), ModuleTaskRequest.ModuleExtraDataInfo.class);
                moduleTaskRequest.setExtraDataInfo(info);
            }
        } catch (IOException e) {
            logger.error("转换moduleExtraInfo异常", e);
        }*/

		return transformModule(module);
	}

	public ModuleTaskRequest transformModule(ETLModule module) {
		ModuleTaskRequest moduleTaskRequest = new ModuleTaskRequest();
		String moduleCode = module.getModuleCode();

		try {
			BeanUtils.copyProperties(module, moduleTaskRequest);
			if (module.getRangeStartDate() != null) {
				moduleTaskRequest.setRangeStartDate(module.getRangeStartDate().getTime());
				moduleTaskRequest.setRangeEndDate(module.getRangeEndDate().getTime());
			}
		} catch (BeansException e) {
			logger.error("转换module异常:", e);
		}

		if (moduleTaskRequest.getRangeStartDate() == null) {
			moduleTaskRequest.setRangeStartDate(0L);
		}
		if (moduleTaskRequest.getRangeEndDate() == null) {
			moduleTaskRequest.setRangeEndDate(0L);
		}
		List<ModuleTaskRequest.InnerWorkflow> workflows = new ArrayList<>();
		moduleTaskRequest.setWorkflows(workflows);
		List<ETLWorkflow> workflowList = module.getWorkflows();
		if (workflowList != null && workflowList.size() > 0) {
			workflowList.stream().sorted(Comparator.comparingInt(ETLWorkflow::getWorkflowSequenceCustomized)).forEach(workflowItem -> {
				ModuleTaskRequest.InnerWorkflow innerWorkflow = new ModuleTaskRequest.InnerWorkflow();
				workflows.add(innerWorkflow);
				//添加workflow 数据
				String workflowCode = workflowItem.getWorkflowCode();
				String lastBefore = StringUtils.substringBeforeLast(workflowCode, "_");
				String tempCode = StringUtils.substringAfterLast(lastBefore, "_") + "_"
					+ StringUtils.substringAfterLast(workflowCode, "_");
				innerWorkflow.setWorkflowCode(StringUtils.substringBefore(tempCode, "_"));
				innerWorkflow.setWorkflowName(workflowItem.getWorkflowName());
				innerWorkflow.setDesc(workflowItem.getWorkflowDesc());
				innerWorkflow.setParamDefine(workflowItem.getParamDefine());
				// code = component_code  +  sequence_default_custom
				innerWorkflow.setWorkflowSequenceDefault(workflowItem.getComponentCode() + "_" +
					(workflowItem.getWorkflowSequenceCustomized() == null ? workflowItem.getWorkflowSequenceDefault() :
						workflowItem.getWorkflowSequenceCustomized())); // workflow_code
				innerWorkflow.setSequenceCustomized(workflowItem.getWorkflowSequenceCustomized());
				innerWorkflow.setCategory(workflowItem.getWorkflowCategory());
				innerWorkflow.setTargetSchema(workflowItem.getTargetSchema());
				innerWorkflow.setTargetTable(workflowItem.getTargetTable());
				innerWorkflow.setLoc(workflowItem.getLoc());
				innerWorkflow.setRunnable(workflowItem.getRunnable());
				innerWorkflow.setIncrementalMode(workflowItem.getIncrementalMode());
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					String workflowParam = StringUtils.isNotEmpty(workflowItem.getWorkflowParam()) ? workflowItem.getWorkflowParam() : "{}";
					innerWorkflow.setParameterJson(objectMapper.readValue(workflowParam, ModuleTaskRequest.WorkflowParamInfo.class));
				} catch (Exception e) {
					logger.error("设置workflow param 出错", e);
				}

				//添加connection
				WorkflowExtraData extraData = new WorkflowExtraData();
				WorkflowExtraData.DisassembleSql sql = new WorkflowExtraData.DisassembleSql();
				innerWorkflow.setExtraData(extraData);
				extraData.setConnection(workflowItem.getConnection());
				extraData.setTargetConnection(workflowItem.getTargetConnection());
				extraData.setSql(sql);
				extraData.setFullSql(workflowItem.getFullSql());
				sql.setSelect(workflowItem.getSelectList().isEmpty() ? new ArrayList<>() : workflowItem
					.getSelectList().stream().peek(selectItem -> {
						if (selectItem.getSourceTableAliasName() != null
							&& selectItem.getSourceColumnName() != null) {
							selectItem.setSourceColumnName(selectItem.getSourceTableAliasName() + "."
								+ selectItem.getSourceColumnName());
						}
					}).collect(Collectors.toList()));
				ETLWorkflowTokenFilter filter = workflowItem.getFilter();
				sql.setFilter(filter);

				if (filter != null) {
					sql.setWhere(filter.getCommonFilterExpressionCustomized());
					sql.setIncrementalWhere(filter.getIncrementalFilterExpressionCustomized());
					sql.setRangeWhere(filter.getRangeFilterExpressionCustomized());
				}

				// 添加reader 和 writer
				List<ETLWorkflowDataxflow> dataxflows = workflowItem.getDataxflows();
				if (dataxflows != null && !dataxflows.isEmpty() || "datax".equalsIgnoreCase(workflowItem.getWorkflowCategory())) {
					innerWorkflow.setIsGroup(true);
					innerWorkflow.setGroupCategory("Group");
					Optional<ETLWorkflowDataxflow> readerItem = dataxflows.stream().filter(d -> d.getDataflowType() == CommonConstant.DataXType.READER.getValue()).findFirst();
					Optional<ETLWorkflowDataxflow> writerItem = dataxflows.stream().filter(d -> d.getDataflowType() == CommonConstant.DataXType.WRITER.getValue()).findFirst();
					readerItem.ifPresent(reader -> {
						WorkflowExtraData.DataFlow readerFlow = new WorkflowExtraData.DataFlow();
						readerFlow.setDataflowCode(reader.getDataflowCode());
						readerFlow.setDataflowName(reader.getDataflowDesc());
						readerFlow.setDataflowType(reader.getDataxflow().getDataflowType());
						readerFlow.setDataflowParam(reader.getDataxflow().getDataflowParam());
						readerFlow.setJdbcType(reader.getDataxflow().getJdbcType());
						try {
							if (reader.getDataflowParameter() != null) {
								readerFlow.setParameter(objectMapper.readValue(reader.getDataflowParameter(), Map.class));
							}
						} catch (IOException e) {
							logger.error("设置dataxflow param 出错", e);
						}
						extraData.setReader(readerFlow);

						ModuleTaskRequest.InnerWorkflow readerWorkflow = new ModuleTaskRequest.InnerWorkflow();
						readerWorkflow.setGroup(innerWorkflow.getWorkflowCode());
						readerWorkflow.setGroupCategory("GroupNode");
						readerWorkflow.setWorkflowCode(innerWorkflow.getWorkflowCode() + "_" + readerFlow.getDataflowCode());
						readerWorkflow.setWorkflowName(readerFlow.getDataflowName());
						readerWorkflow.setWorkflowSequenceDefault(tempCode);
						readerWorkflow.setColor("lightgreen");
						readerWorkflow.setParamDefine(workflowItem.getParamDefine());
						readerWorkflow.setCategory(workflowItem.getWorkflowCategory());
						readerWorkflow.setDataFlowType(CommonConstant.DataXType.READER.getValue());
						workflows.add(readerWorkflow);

					});
					// 如果没有，设置为默认
					if (!readerItem.isPresent()) {
						ModuleTaskRequest.InnerWorkflow readerWorkflow = new ModuleTaskRequest.InnerWorkflow();
						readerWorkflow.setGroup(innerWorkflow.getWorkflowCode());
						readerWorkflow.setGroupCategory("GroupNode");
						readerWorkflow.setWorkflowCode(innerWorkflow.getWorkflowCode() + "_reader");
						readerWorkflow.setWorkflowName("读取");
						readerWorkflow.setWorkflowSequenceDefault(tempCode);
						readerWorkflow.setColor("lightgreen");
						readerWorkflow.setParamDefine(workflowItem.getParamDefine());
						readerWorkflow.setCategory(workflowItem.getWorkflowCategory());
						readerWorkflow.setDataFlowType(CommonConstant.DataXType.READER.getValue());
						workflows.add(readerWorkflow);
					}
					writerItem.ifPresent(writer -> {
						WorkflowExtraData.DataFlow writerFlow = new WorkflowExtraData.DataFlow();
						writerFlow.setDataflowCode(writer.getDataflowCode());
						writerFlow.setDataflowName(writer.getDataflowDesc());
						writerFlow.setDataflowType(writer.getDataxflow().getDataflowType());
						writerFlow.setDataflowParam(writer.getDataxflow().getDataflowParam());
						writerFlow.setJdbcType(writer.getDataxflow().getJdbcType());
						try {
							if (writer.getDataflowParameter() != null) {
								writerFlow.setParameter(objectMapper.readValue(writer.getDataflowParameter(), Map.class));
							}
						} catch (IOException e) {
							logger.error("设置dataxflow param 出错", e);
						}
						extraData.setWriter(writerFlow);

						ModuleTaskRequest.InnerWorkflow writerWorkflow = new ModuleTaskRequest.InnerWorkflow();
						writerWorkflow.setGroup(innerWorkflow.getWorkflowCode());
						writerWorkflow.setGroupCategory("GroupNode");
						writerWorkflow.setWorkflowCode(innerWorkflow.getWorkflowCode() + "_" + writerFlow.getDataflowCode());
						writerWorkflow.setWorkflowName(writerFlow.getDataflowName());
						writerWorkflow.setWorkflowSequenceDefault(tempCode);
						writerWorkflow.setColor("pink");
						writerWorkflow.setParamDefine(workflowItem.getParamDefine());
						writerWorkflow.setCategory(workflowItem.getWorkflowCategory());
						writerWorkflow.setDataFlowType(CommonConstant.DataXType.WRITER.getValue());
						workflows.add(writerWorkflow);
					});
					if (!writerItem.isPresent()) {
						ModuleTaskRequest.InnerWorkflow writerWorkflow = new ModuleTaskRequest.InnerWorkflow();
						writerWorkflow.setGroup(innerWorkflow.getWorkflowCode());
						writerWorkflow.setGroupCategory("GroupNode");
						writerWorkflow.setWorkflowCode(innerWorkflow.getWorkflowCode() + "_writer");
						writerWorkflow.setWorkflowName("输出");
						writerWorkflow.setWorkflowSequenceDefault(tempCode);
						writerWorkflow.setColor("pink");
						writerWorkflow.setParamDefine(workflowItem.getParamDefine());
						writerWorkflow.setCategory(workflowItem.getWorkflowCategory());
						writerWorkflow.setDataFlowType(CommonConstant.DataXType.WRITER.getValue());
						workflows.add(writerWorkflow);
					}
				}

				WorkflowExtraData.DisassembleFrom from = new WorkflowExtraData.DisassembleFrom();
				if (!workflowItem.getFromOrJoinList().isEmpty()) {
					List<ETLWorkflowTokenFromOrJoin> joinTables = new ArrayList<>();
					Optional.ofNullable(workflowItem.getFromOrJoinList()).ifPresent(froms -> {
						joinTables.addAll(froms.stream().peek(p -> {
							if (p.getIsPrimaryTable() != null && p.getIsPrimaryTable() == 1) {
								from.setPrimaryTable(p);
							}
						}).filter(f -> f.getIsPrimaryTable() == null).collect(Collectors.toList()));
						from.setJoinTables(joinTables);
					});
				}
				sql.setFrom(from);
			});
		}
		return moduleTaskRequest;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean editEtlModule(ModuleTaskRequest module) {

		// 判断是新增/ 修改
		boolean exist = StringUtils.isNotEmpty(module.getModuleCode());

		Optional<ETLModule> etlModule = transform(module);

		String moduleCode = StringUtils.isEmpty(module.getModuleCode()) ? "ETL_" + module.getModuleCategory()
			+ "_" + module.getTargetTable() + "_" + module.getHospitalName()
			+ "_" + System.currentTimeMillis() : module.getModuleCode();

		module.setModuleCode(moduleCode);
		//组件
		List<ETLWorkflow> workflows = transformWorkflow(module, exist);

		etlModule.ifPresent(etlModuleItem -> {


            if (exist) {
                DipConfig.getConfigInstance().clearConfigCache("module");
                //修改module数据
                etlModuleItem.setCreatedAt(null);
                mapper.updateByPrimaryKeySelective(etlModuleItem);

				// 修改module 对应job
				jobModuleMapper.updateJobIdByModuleCode(etlModuleItem.getJobId(), moduleCode);


				fullSqlMapper.removeWorkflowTokenFullSqlByModuleCode(moduleCode);
				fromOrJoinMapper.removeWorkflowTokenFromByModuleCode(moduleCode);
				filterMapper.removeWorkflowTokenFilter(moduleCode);
				selectMapper.removeETLWorkflowTokenSelectByModuleCode(moduleCode);
				workflowTokenMapper.removeEtlWorkflowTokenByModuleCode(moduleCode);
				workflowConnectionMapper.removeEtlWorkflowConnectionByModuleCode(moduleCode);
				workflowMapper.removeEtlWorkflowByModuleCode(moduleCode);

				List<String> workflowCodeList = workflows.stream()
					.map(ETLWorkflow::getWorkflowCode).collect(Collectors.toList());
				// 删除 workflow connection
				Example example = new Example(ETLWorkflowConnection.class);
				example.createCriteria().andIn("workflowCode", workflowCodeList);
				workflowConnectionMapper.deleteByExample(example);
				// 删除workflow_dataxflow
				Example example2 = new Example(ETLWorkflowDataxflow.class);
				example2.createCriteria().andIn("workflowCode", workflowCodeList);

				workflowDataxflowMapper.deleteByExample(example2);

			} else {
				// 保存module
				etlModuleItem.setModuleCode(moduleCode);
				mapper.insert(etlModuleItem);

				// 保存module_job
				jobModuleMapper.insert(new EtlJobModule(null, etlModuleItem.getJobId(), moduleCode));

			}


            Optional.ofNullable(workflows).ifPresent(wfs -> {

                wfs.forEach(workflow -> {

                    workflow.setUpdatedAt(new Date());
                    workflow.setIsEnable(1);
                    workflow.setIsDefault(1);
                    workflow.setModuleCode(moduleCode);
                    if (exist) {
                        int i = workflowMapper.updateByPrimaryKeySelective(workflow); // 修改。如果没有修改的数据，那就是新增的
                        if (i == 0) {
                            workflow.setCreatedAt(new Date());
                            workflowMapper.insert(workflow);
                        }
                        //修改workflow_token 状态
                        workflowTokenMapper.updateEnableStatusByWorkflowCode(0, workflow.getWorkflowCode());
                    } else {
                        //保存workflow
                        workflow.setCreatedAt(new Date());
                        workflow.setIsDefault(1);
                        workflowMapper.insert(workflow);
                    }

					// 保存workflow_connection: 源 或者 目标
					if (workflow.getConnection() != null
						&& StringUtils.isNotEmpty(workflow.getConnection().getConnectionCode())) {
						// 没有设置目标源，那就用一个源就是all
						if (workflow.getTargetConnection() == null
							|| StringUtils.isEmpty(workflow.getTargetConnection().getConnectionCode())) {
							workflowConnectionMapper.insert(new ETLWorkflowConnection(null, workflow.getWorkflowCode(),
								workflow.getConnection().getConnectionCode(), "all"));
						} else {
							workflowConnectionMapper.insert(new ETLWorkflowConnection(null, workflow.getWorkflowCode(),
								workflow.getConnection().getConnectionCode(), "source"));
							workflowConnectionMapper.insert(new ETLWorkflowConnection(null, workflow.getWorkflowCode(),
								workflow.getTargetConnection().getConnectionCode(), "target"));
						}
					}
					// 保存workflow dataxflow组件
					if (workflow.getDataxflows() != null) {
						for (ETLWorkflowDataxflow datax : workflow.getDataxflows()) {
							if (!exist) {
								datax.setCreatedAt(new Date());
							}
							datax.setUpdatedAt(new Date());
							workflowDataxflowMapper.insert(datax);
						}
					}

					// full_sql 如果有，就不用SQL拼接的那种
					if (StringUtils.isNotEmpty(workflow.getFullSql())) {
						String fullSqlTokenCode = "ETL_FULL_SQL";

						ETLWorkflowToken token = new ETLWorkflowToken();
						token.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + fullSqlTokenCode);
						token.setTokenCode(fullSqlTokenCode);
						token.setWorkflowCode(workflow.getWorkflowCode());
						token.setIsDefault(0);
						token.setIsEnable(1);
						token.setIsDefault(1);
						token.setUpdatedAt(new Date());
						boolean needCreate = true; // 是否需要新增数据
						if (exist) {
							if (workflowTokenMapper.updateByPrimaryKeySelective(token) > 0) {
								needCreate = false;
							}
						}
						if (needCreate) {
							token.setCreatedAt(new Date());
							workflowTokenMapper.insert(token);
						}

						ETLWorkflowTokenFullSql fullSqlToken = new ETLWorkflowTokenFullSql();
						fullSqlToken.setFullSqlDefault(workflow.getFullSql());
						fullSqlToken.setFullSqlCustomized(workflow.getFullSql());
						fullSqlToken.setIsEnable(1);
						fullSqlToken.setIsDefault(1);
						fullSqlToken.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + fullSqlTokenCode);
						fullSqlToken.setUpdatedAt(new Date());
						needCreate = true;
						if (exist) {
							if (fullSqlMapper.updateByPrimaryKeySelective(fullSqlToken) > 0) {
								needCreate = false;
							}
						}
						if (needCreate) {
							fullSqlToken.setCreatedAt(new Date());
							fullSqlMapper.insert(fullSqlToken);
						}
					} else {

						// select
						Optional.ofNullable(workflow.getSelectList()).ifPresent(selectList -> {

							String selectTokenCode = "ETL_SOURCE_SELECT_COLUMNS_DEFAULT";
							ETLWorkflowToken token = new ETLWorkflowToken();
							token.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + selectTokenCode);
							token.setTokenCode(selectTokenCode);
							token.setWorkflowCode(workflow.getWorkflowCode());
							token.setIsEnable(1);
							token.setIsDefault(1);
							token.setUpdatedAt(new Date());
							boolean selectNeedCreate = true; //判断修改的时候是否有新增数据
							if (exist) {
								if (workflowTokenMapper.updateByPrimaryKeySelective(token) > 0) {
									selectNeedCreate = false;
								}
							}
							if (selectNeedCreate) {
								token.setCreatedAt(new Date());
								workflowTokenMapper.insert(token);
							}

							//修改所有select_token对应的状态
							if (exist) {
								selectMapper.updateEnableStatusByCode(0, workflow.getWorkflowCode() + "_" + selectTokenCode);
							}

							selectList.forEach(select -> {
								String sourceColumn = select.getSourceColumnName();
								if (StringUtils.isNotEmpty(sourceColumn)) {
									select.setSourceTableAliasName(StringUtils.substringBefore(sourceColumn, "."));
									select.setSourceColumnName(StringUtils.substringAfterLast(sourceColumn, "."));
								}
								if (!exist && StringUtils.isNotEmpty(select.getSourceColumnExpressionCustomized())) {
									select.setSourceColumnExpressionDefault(select.getSourceColumnExpressionCustomized());
								}
								select.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + selectTokenCode);
								select.setUpdatedAt(new Date());
								select.setIsEnable(1);
								select.setIsDefault(1);
								boolean needCreateSelect = true;
								if (exist) {
									if (selectMapper.updateByPrimaryKeySelective(select) > 0) {
										needCreateSelect = false;
									}
								}
								if (needCreateSelect) {
									select.setCreatedAt(new Date());
									selectMapper.insert(select);
								}

							});
						});

						// from :
						// 先禁用token对应的所有的from
						String fromTokenCode = "ETL_SOURCE_TABLES_DEFAULT";
						if (exist) {
							fromOrJoinMapper.updateEnableStatusByCode(0, workflow.getWorkflowCode() + "_" + fromTokenCode);
						}

						Optional.ofNullable(workflow.getFromOrJoinList()).ifPresent(fromJoins -> {

							ETLWorkflowToken token = new ETLWorkflowToken();
							token.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + fromTokenCode);
							token.setTokenCode(fromTokenCode);
							token.setWorkflowCode(workflow.getWorkflowCode());
							token.setIsEnable(1);
							token.setIsDefault(1);
							token.setUpdatedAt(new Date());
							boolean fromNeedCreate = true;
							if (exist) {
								if (workflowTokenMapper.updateByPrimaryKeySelective(token) > 0) {
									fromNeedCreate = false;
								}
							}
							if (fromNeedCreate) {
								token.setCreatedAt(new Date());
								workflowTokenMapper.insert(token);
							}

							fromJoins.forEach(from -> {
								from.setUpdatedAt(new Date());
								from.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + fromTokenCode);
								from.setIsEnable(1);
								from.setIsDefault(1);
								boolean needCreateFrom = true;
								if (exist) {
									if (fromOrJoinMapper.updateByPrimaryKeySelective(from) > 0) {
										needCreateFrom = false;
									}
								}
								if (needCreateFrom) {
									from.setCreatedAt(new Date());
									fromOrJoinMapper.insert(from);
								}
							});
						});
					}

					// where
					String whereTokenCode = "ETL_SOURCE_WHERE_DEFAULT";
					if (exist) {
						workflowTokenMapper.updateEnableStatusByWorkflowCode(0, workflow.getWorkflowCode() + "_" + whereTokenCode);
					}
					Optional.ofNullable(workflow.getFilter()).ifPresent(where -> {
						if (StringUtils.isNotEmpty(where.getCommonFilterExpression()) ||
							StringUtils.isNotEmpty(where.getCommonFilterExpressionCustomized()) ||
							StringUtils.isNotEmpty(where.getIncrementalFilterExpression()) ||
							StringUtils.isNotEmpty(where.getIncrementalFilterExpressionCustomized()) ||
							StringUtils.isNotEmpty(where.getRangeFilterExpression()) ||
							StringUtils.isNotEmpty(where.getRangeFilterExpressionCustomized())) {
							ETLWorkflowToken token = new ETLWorkflowToken();
							token.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + whereTokenCode);
							token.setTokenCode(whereTokenCode);
							token.setWorkflowCode(workflow.getWorkflowCode());
							token.setIsEnable(1);
							token.setIsDefault(1);
							token.setUpdatedAt(new Date());
							boolean whereNeedCreate = true;
							if (exist) {
								if (workflowTokenMapper.updateByPrimaryKeySelective(token) > 0) {
									whereNeedCreate = false;
								}
							}
							if (whereNeedCreate) {
								token.setCreatedAt(new Date());
								workflowTokenMapper.insert(token);
							}

							where.setUpdatedAt(new Date());
							where.setCommonFilterExpression(where.getCommonFilterExpressionCustomized());
							where.setIncrementalFilterExpression(where.getIncrementalFilterExpressionCustomized());
							where.setRangeFilterExpression(where.getRangeFilterExpressionCustomized());
							where.setWorkflowTokenCode(workflow.getWorkflowCode() + "_" + whereTokenCode);
							where.setIsEnable(1);
							where.setIsDefault(1);
							where.setCreatedAt(new Date());
							filterMapper.insert(where);
						}
					});

                    //修改状态
					versionMapper.updateWorkflowCodeByVersionStatus(workflow.getWorkflowCode());
                    //记录版本
					JobVersion version = new JobVersion();
					version.setVersionCode(UUID.randomUUID().toString());
					version.setCreateDate(new Date());
					version.setUserId(SecurityUtils.getUser().getId());
					version.setWorkflowCode(workflow.getWorkflowCode());
					version.setWorkflowSql(versionMapper.selectViewSql(workflow.getWorkflowCode()));
					versionMapper.insert(version);
                });


            });
        });


		return true;
	}

	private Optional<ETLModule> transform(ModuleTaskRequest moduleTaskRequest) {
		ETLModule etlModule = new ETLModule();
		if (moduleTaskRequest == null) {
			return Optional.empty();
		}
		etlModule.setModuleName(moduleTaskRequest.getModuleName());
		etlModule.setModuleDescription(moduleTaskRequest.getModuleDescription());
		etlModule.setModuleCode(moduleTaskRequest.getModuleCode());
		etlModule.setTargetSchema(moduleTaskRequest.getTargetSchema());
		etlModule.setTargetTable(moduleTaskRequest.getTargetTable());
		etlModule.setModulePriority(moduleTaskRequest.getModulePriority());
		etlModule.setEtlType(moduleTaskRequest.getEtlType());
		etlModule.setModuleCategory(moduleTaskRequest.getModuleCategory());
		etlModule.setEnabled(moduleTaskRequest.getEnabled());
		etlModule.setFullWhileMonths(moduleTaskRequest.getFullWhileMonths() == null ? 0 : moduleTaskRequest.getFullWhileMonths());
		etlModule.setRangeStartDate(moduleTaskRequest.getRangeStartDate() == 0 ? null : new Date(moduleTaskRequest.getRangeStartDate()));
		etlModule.setRangeEndDate(moduleTaskRequest.getRangeEndDate() == 0 ? null : new Date(moduleTaskRequest.getRangeEndDate()));
		etlModule.setHospitalName(moduleTaskRequest.getHospitalName());
		//etlModule.setTopicId(moduleTaskRequest.getTopicId() == null ? 0 : moduleTaskRequest.getTopicId());
		etlModule.setConnectionCode(moduleTaskRequest.getConnectionCode());
		etlModule.setCreatedAt(moduleTaskRequest.getCreatedAt());
		etlModule.setUpdatedAt(moduleTaskRequest.getUpdatedAt());
		etlModule.setJobId(moduleTaskRequest.getJobId() == null ? 0 : moduleTaskRequest.getJobId());
        /*if (null != moduleTaskRequest.getExtraDataInfo()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            try {
                etlModule.setModuleJsonInfo(objectMapper.writeValueAsString(moduleTaskRequest.getExtraDataInfo()));
            } catch (IOException e) {
                logger.error("设置module extraDataInfo的值出错", e);
            }
        }*/
		return Optional.of(etlModule);
	}

	/**
	 * 前台json数据格式转换数据库workflow保存 , sql 语句的分解
	 *
	 * @param moduleTaskRequest 前台页面数据封装对象
	 * @param exist             存在->修改，不存在-> 新增
	 * @return workflow 集合
	 */
	private List<ETLWorkflow> transformWorkflow(ModuleTaskRequest moduleTaskRequest, boolean exist) {
		List<ModuleTaskRequest.InnerWorkflow> innerWorkflowList = moduleTaskRequest.getWorkflows();
		// 过滤掉组合元素中的reader 和 writer
		innerWorkflowList = innerWorkflowList.stream()
			.filter(workflowItem -> StringUtils.isEmpty(workflowItem.getGroup())).collect(Collectors.toList());
		int innerWorkflowListSize = innerWorkflowList.size();
		List<ETLWorkflow> workflows = new ArrayList<>(innerWorkflowListSize);
		for (int i = 0; i < innerWorkflowListSize; i++) {
			ModuleTaskRequest.InnerWorkflow iwf = innerWorkflowList.get(i);
			ETLWorkflow workflow = new ETLWorkflow();
			workflows.add(workflow);

			workflow.setWorkflowCode(moduleTaskRequest.getModuleCode()
				+ "_" + iwf.getWorkflowCode() + "_" + (i + 1));
			workflow.setWorkflowName(iwf.getWorkflowName());
			workflow.setComponentCode(StringUtils.substringBefore(iwf
				.getWorkflowSequenceDefault(), "_"));
			workflow.setModuleCode(moduleTaskRequest.getModuleCode());
			workflow.setWorkflowSequenceDefault(Integer
				.parseInt(StringUtils.substringAfterLast(iwf
					.getWorkflowSequenceDefault(), "_")));
			workflow.setWorkflowSequenceCustomized(iwf.getSequenceCustomized());
			workflow.setTargetSchema(iwf.getTargetSchema());
			workflow.setTargetTable(iwf.getTargetTable());
			workflow.setLoc(iwf.getLoc());
			workflow.setRunnable(iwf.getRunnable());
			workflow.setIncrementalMode(iwf.getIncrementalMode());
			if (iwf.getParameterJson() != null) {
				// 设置workflow 参数：
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
					workflow.setWorkflowParam(objectMapper.writeValueAsString(iwf.getParameterJson()));
				} catch (JsonProcessingException e) {
					logger.error("设置workflow param的值出错", e);
					throw new RuntimeException("读取workflow参数出错");
				}
			}
			List<ETLWorkflowDataxflow> dataxflowList = new ArrayList<>();
			workflow.setDataxflows(dataxflowList);
			// 添加workflow_dataxflow
			if (iwf.getExtraData().getReader() != null) {
				WorkflowExtraData.DataFlow reader = iwf.getExtraData().getReader();
				ETLWorkflowDataxflow dataxflow = new ETLWorkflowDataxflow();
				dataxflow.setWorkflowCode(workflow.getWorkflowCode());
				dataxflow.setDataflowCode(reader.getDataflowCode());
				dataxflow.setDataflowDesc(reader.getDataflowName());
				dataxflow.setDataflowType(CommonConstant.DataXType.READER.getValue());
				try {
					if (reader.getParameter() != null) {
						ObjectMapper objectMapper = new ObjectMapper();
						dataxflow.setDataflowParameter(objectMapper.writeValueAsString(reader.getParameter()));
					}
				} catch (JsonProcessingException e) {
					logger.error("读取reader param的值出错", e);
				}
				dataxflowList.add(dataxflow);
			}
			if (iwf.getExtraData().getWriter() != null) {
				WorkflowExtraData.DataFlow writer = iwf.getExtraData().getWriter();
				ETLWorkflowDataxflow dataxflow = new ETLWorkflowDataxflow();
				dataxflow.setWorkflowCode(workflow.getWorkflowCode());
				dataxflow.setDataflowCode(writer.getDataflowCode());
				dataxflow.setDataflowDesc(writer.getDataflowName());
				dataxflow.setDataflowType(CommonConstant.DataXType.WRITER.getValue());
				try {
					if (writer.getParameter() != null) {
						ObjectMapper objectMapper = new ObjectMapper();
						dataxflow.setDataflowParameter(objectMapper.writeValueAsString(writer.getParameter()));
					}
				} catch (JsonProcessingException e) {
					logger.error("读取writer param的值出错", e);
				}
				dataxflowList.add(dataxflow);
			}

			WorkflowExtraData.DisassembleSql sql = iwf.getExtraData().getSql();
			workflow.setSelectList(sql.getSelect().size() == 0 ? null : sql.getSelect());
			List<ETLWorkflowTokenFromOrJoin> fromList = new ArrayList<>();

			String primaryTableName = sql.getFrom().getPrimaryTable() == null ? "" :
				StringUtils.defaultIfEmpty(sql.getFrom().getPrimaryTable().getSourceTableName(), sql.getFrom().getPrimaryTable().getSourceTableExpression());
			if (StringUtils.isNotEmpty(primaryTableName)) {
				fromList.add(sql.getFrom().getPrimaryTable());
			}
			if (sql.getFrom().getJoinTables() != null && sql.getFrom().getJoinTables().size() > 0) {
				fromList.addAll(sql.getFrom().getJoinTables());
			}
			workflow.setFromOrJoinList(fromList);
			//判断etl类型，设置不同的where条件
			ETLWorkflowTokenFilter filter = new ETLWorkflowTokenFilter();
			if (!exist) { // 编辑时，只修改customized 的值
				filter.setCommonFilterExpression(sql.getWhere());
				filter.setIncrementalFilterExpression(sql.getIncrementalWhere());
				filter.setRangeFilterExpression(sql.getRangeWhere());
			}
			filter.setCommonFilterExpressionCustomized(StringUtils.isEmpty(sql.getWhere()) ? null : sql.getWhere());
			filter.setIncrementalFilterExpressionCustomized(StringUtils.isEmpty(sql.getIncrementalWhere()) ? null : sql.getIncrementalWhere());
			filter.setRangeFilterExpressionCustomized(StringUtils.isEmpty(sql.getRangeWhere()) ? null : sql.getRangeWhere());

			workflow.setFilter(filter);
			if (iwf.getExtraData().getConnection() != null && StringUtils.isNotEmpty(iwf.getExtraData().getConnection().getConnectionCode())) {
				workflow.setConnection(iwf.getExtraData().getConnection());
			}
			if (iwf.getExtraData().getTargetConnection() != null && StringUtils.isNotEmpty(iwf.getExtraData().getTargetConnection().getConnectionCode())) {
				workflow.setTargetConnection(iwf.getExtraData().getTargetConnection());
			}
			workflow.setFullSql(iwf.getExtraData().getFullSql());
		}
		return workflows;
	}

	public int countModuleByTopicId(Integer topicId, Integer jobId) {
		return moduleMapper.selectModuleByTopicId(topicId, jobId);
	}

	public void renovateModuleStatus(String code, Integer enabled) {
		moduleMapper.renovateModuleStatus(code, enabled);
	}

	public ETLLogSummary getLogsInfoByModule(String module, String runtime) {
		return moduleMapper.getLogsInfoByModule(module, runtime);
	}

	public int deleteModuleByModuleCode(String moduleCode) throws Exception {
		EtlJobModule jobModule = new EtlJobModule();
		jobModule.setModuleCode(moduleCode);
		EtlJobModule module = jobModuleMapper.selectOne(jobModule);
		this.cancelModuleByModuleCode(moduleCode);
		return module.getJobId();
	}

	@Transactional
	public void cancelModuleByModuleCode(String moduleCode) throws Exception {
		DipConfig.getConfigInstance().clearConfigCache("module");
		jobModuleMapper.removeETLJobModuleByModuleCode(moduleCode);
		workflowDataxflowMapper.removeWorkflowDataXFlow(moduleCode);
		fullSqlMapper.removeWorkflowTokenFullSqlByModuleCode(moduleCode);
		fromOrJoinMapper.removeWorkflowTokenFromByModuleCode(moduleCode);
		filterMapper.removeWorkflowTokenFilter(moduleCode);
		selectMapper.removeETLWorkflowTokenSelectByModuleCode(moduleCode);
		workflowTokenMapper.removeEtlWorkflowTokenByModuleCode(moduleCode);
		workflowConnectionMapper.removeEtlWorkflowConnectionByModuleCode(moduleCode);
		workflowMapper.removeEtlWorkflowByModuleCode(moduleCode);
		moduleMapper.deleteByPrimaryKey(moduleCode);
	}

	public ETLModule checkModuleCode(String moduleCode) {
		return moduleMapper.selectByPrimaryKey(moduleCode);
	}

	public List<ETLHospital> queryHospitals() {
		return moduleMapper.queryHospitals();
	}

	@Transactional(rollbackFor = Exception.class)
	public int editEtlHospital(ETLHospital hospital) throws Exception {
		Boolean flag = true;
		List<ETLHospital> hospitals = this.queryHospitals();
		for (ETLHospital etlHospital : hospitals) {
			if (etlHospital.getHospitalCode().equals(hospital.getHospitalCode())) {
				flag = false;
			}
		}
		if (flag) {
			return moduleMapper.insertHospital(hospital);
		}
		return moduleMapper.updateHospital(hospital);
	}

	public void removeHospitalByCode(String hospitalCode) {
		moduleMapper.deleteHospital(hospitalCode);
	}

	public int updateModuleByCode(ModuleTaskRequest moduleTask) throws Exception {
		ETLModule module = new ETLModule();
		module.setModuleCode(moduleTask.getModuleCode());
		module.setEtlType(moduleTask.getEtlType());
		if (moduleTask.getFullWhileMonths() != null) {
			module.setFullWhileMonths(moduleTask.getFullWhileMonths());
		}

		if (moduleTask.getRangeStartDate() == null || moduleTask.getRangeStartDate() == 0) {
			module.setRangeStartDate(null);
		} else {
			module.setRangeStartDate(new Date(moduleTask.getRangeStartDate()));
		}

		if (moduleTask.getRangeEndDate() == null || moduleTask.getRangeEndDate() == 0) {
			module.setRangeEndDate(null);
		} else {
			module.setRangeEndDate(new Date(moduleTask.getRangeEndDate()));
		}
		module.setNumberRange(StringUtils.defaultIfEmpty(moduleTask.getNumberRange(), null));
		return moduleMapper.updateModuleByCode(module);
	}

	public int updatePriorityByModuleCode(String moduleCode, Integer priority) {
		ETLModule etlModule = new ETLModule();
		etlModule.setModuleCode(moduleCode);
		etlModule.setModulePriority(priority);
		etlModule.setEtlType(null);
		etlModule.setEnabled(null);
		etlModule.setFullWhileMonths(null); // 设置null是为了不往表里面插入数据
		return moduleMapper.updateByPrimaryKeySelective(etlModule);
	}

	public void moveModuleByJob(String moduleCode, Integer JobId) throws SQLException {
		Example example = new Example(EtlJobModule.class);
		example.createCriteria().andEqualTo("moduleCode", moduleCode);
		EtlJobModule jobModule = new EtlJobModule();
		jobModule.setJobId(JobId);
		jobModuleMapper.updateByExampleSelective(jobModule, example);
	}

	public int batchOperation(List<String> moduleCodeList, boolean isDisable) {
		Example example = new Example(ETLModule.class);
		example.createCriteria().andIn("moduleCode", moduleCodeList);
		ETLModule etlModule = new ETLModule();
		etlModule.setModulePriority(null);
		etlModule.setEtlType(null);
		etlModule.setEnabled(isDisable ? 0 : 1);
		etlModule.setFullWhileMonths(null); // 设置null是为了不往表里面插入数据
		return moduleMapper.updateByExampleSelective(etlModule, example);
	}

	public int batchFullOrIncrement(List<String> moduleCodeList, boolean isFullOrIncrement) {
		Example example = new Example(ETLModule.class);
		example.createCriteria().andIn("moduleCode", moduleCodeList);
		ETLModule etlModule = new ETLModule();
		etlModule.setModulePriority(null);
		etlModule.setEtlType(isFullOrIncrement ? 0 : 1);
		etlModule.setFullWhileMonths(0);
		etlModule.setEnabled(null);
		etlModule.setRangeStartDate(null);
		etlModule.setRangeEndDate(null);
		return moduleMapper.updateByExampleSelective(etlModule, example);
	}

	public List<Map> selectModuleCodeByWorkflowInfo() {
		return moduleMapper.selectModuleCodeByWorkflowInfo();
	}

	@Async("taskExecutor")
	public void execModule(String moduleCode, String uuid) throws Exception {
		logger.info("execModule. exec。。。");
		ETLStart.startByModule(moduleCode, uuid);
	}

	public PageResult<Entity> execCheckDataModule(String moduleCode, String workflowCode,
												  String uuid, Page pageItem) throws Exception {
		logger.info("数据预览，核查任务...");
		Preconditions.checkNotNull(checkDataUrl, "核查系统的访问地址为空");
		Map<String, Object> paramMap = new HashMap<>();
		// 先禁用任务
		renovateModuleStatus(moduleCode, 1);
		// 编辑核查点
		if(editWorkflowPoint(workflowCode) > 0) {
			ETLStart.startCheckDataByModule(moduleCode, uuid, paramMap);
		}
		// 执行完没有问题就查询module目标表
		return getList(moduleCode, pageItem);
	}

	private int editWorkflowPoint(String workflowCode) {
		return workflowMapper.editWorkflowCheckPoint(workflowCode);
	}

	public PageResult<Entity> getList(String moduleCode, Page pageItem) throws Exception{
		PageResult<Entity> pageList = new PageResult<>();
		final ETLModule etlModule = moduleMapper.selectByPrimaryKey(moduleCode);

		final ETLConnection etlConnection = connectionService.selectOne(etlModule.getConnectionCode());
		if (etlConnection != null) {
			String tableName = etlModule.getTargetSchema() + "."
				+ (StringUtils.endsWithIgnoreCase(etlModule.getTargetTable(), "_import") ? etlModule.getTargetTable() : etlModule.getTargetTable() + "_import");
			IDatabaseClient databaseClient = DatabaseClientFactory.getDatabaseClient(etlConnection.getUrl(), etlConnection.getUser(), etlConnection.getPassword());

			pageList = Db.use(databaseClient.getDataSource())
				.page(Entity.create(tableName), pageItem);
		}
		return pageList;
	}

	public String startCheck(String moduleCode,String startTime, String endTime, String uuid) throws Exception {

		final ETLModule module = moduleMapper.selectByPrimaryKey(moduleCode);

		Weekend<ETLLogSummary> weekend = new Weekend<>(ETLLogSummary.class);
		weekend.weekendCriteria().andEqualTo(ETLLogSummary::getBatchId, uuid);
		final ETLLogSummary logSummary = logSummaryMapper.selectOneByExample(weekend);

		final ETLConnection etlConnection = connectionService.selectOne(module.getConnectionCode());
		Map<String, Object> paramMap = new HashMap<>();
		String tableName = module.getTargetTable();
		if(tableName.contains(".")) {
			tableName = StringUtils.substringAfter(tableName,".");
		}
		paramMap.put("table", tableName); // 表
		paramMap.put("url", etlConnection.getUrl()); // 表
		paramMap.put("startTime", StringUtils.defaultIfEmpty(startTime, DateUtil.formatDateTime(DateUtil.lastMonth()))); // 表
		paramMap.put("endTime", StringUtils.defaultIfEmpty(endTime, DateUtil.formatDateTime(DateUtil.date()))); // 表

		String result = "";
		String httpResult = "";
		try {
			httpResult = HttpUtil.get(checkDataUrl + "etl/check/", paramMap);
			final CheckResult<String> checkResult = JSONUtil.toBean(httpResult, CheckResult.class);

			Preconditions.checkArgument(checkResult.getCode() == 200, "调用数据核查服务出错," + httpResult);
			logSummary.setCheckId(checkResult.getMsg());
			result = checkResult.getMsg();

			logSummaryMapper.updateByExample(logSummary, weekend);
		} catch (Exception e) {
			logger.error("调用数据核查服务出错", e);
			throw new RuntimeException(e.getMessage() + httpResult);
		}

		return result;
	}

	public String checkResult(String uuid) {

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("batchno", uuid); // 表
		return HttpUtil.get(checkDataUrl + "etl/scope/", paramMap);
	}

	@Getter
	@Setter
	public static class CheckResult<T> {
		private String msg;
		private Integer code;
		private T data;
	}
}
