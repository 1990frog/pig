package com.clinbrain.dip.strategy.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.connection.DatabaseMeta;
import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.pojo.ETLWorkflow;
import com.clinbrain.dip.pojo.ETLWorkflowTokenFromOrJoin;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.rest.service.ConnectionService;
import com.clinbrain.dip.strategy.entity.JobVersion;
import com.clinbrain.dip.strategy.mapper.VersionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (TTemplet)表服务实现类
 *
 * @author Liaopan
 * @since 2020-09-04 10:48:37
 */
@Service
@RequiredArgsConstructor
public class VersionService extends BaseService<JobVersion> {

	private final VersionMapper mapper;
	private final DBETLModuleMapper dbmapper;

	private final ConnectionService connectionService;

	public IPage selectVersionList(Page page, String workCode) {
		return mapper.selectVersionPage(page, workCode);
	}


	public void updateWorkflowCodeByVersionStatus(String workCode) {
		mapper.updateWorkflowCodeByVersionStatus(workCode);
	}


	public String selectWorkFlowSql(ETLWorkflow workflow) {

		//获取connect
		ETLConnection connection = dbmapper.selectTargetConnection(workflow.getWorkflowCode());
		Map<String, String> colMap = new HashMap<>();
		if (connection == null) {
			return workflow.getFullSql();
		}
		//获取元数据字段
		List<DatabaseMeta> databaseMetaList = connectionService.getDataBases(connection.getConnectionCode(), workflow.getTargetSchema(), workflow.getTargetTable());
		Optional.ofNullable(databaseMetaList).ifPresent(databases -> databases.forEach(d -> {
			Optional.ofNullable(d.getTableMetas()).ifPresent(tables -> tables.forEach(t -> {
				Optional.ofNullable(t.allColumns).ifPresent(cols -> cols.forEach(c -> {
					colMap.put(c.name.toUpperCase(), c.comment);
				}));
			}));
		}));


		StringBuilder sb = new StringBuilder("select ");
		final String selectColumns = workflow.getSelectList().stream().filter(s -> 1 == s.getIsEnable()).map(select -> {
			String selectColumn = Optional.ofNullable(select.getSourceColumnExpressionCustomized()).orElse(select.getSourceColumnExpressionDefault());
			if (StringUtils.isEmpty(selectColumn)) {
				selectColumn = select.getSourceTableAliasName() + "." + select.getSourceColumnName();
			}

			return selectColumn + " AS " + select.getTargetColumnAliasName()
				+ (StringUtils.isNotEmpty(colMap.get(StringUtils.upperCase(select.getTargetColumnAliasName()))) ? " -- "
				+ colMap.get(StringUtils.upperCase(select.getTargetColumnAliasName())) :"")
				+ "\r\n";
		}).collect(Collectors.joining(","));
		sb.append(selectColumns).append(" FROM ");  // 添加select
		// 添加 主表from
		StringBuilder primaryTableWhere = new StringBuilder("");
		workflow.getFromOrJoinList().stream().filter(w -> 1 == w.getIsPrimaryTable() && 1 == w.getIsEnable())
			.findFirst().ifPresent(w -> {
			sb.append(Optional.ofNullable(w.getSourceTableExpression()).orElse(w.getSourceDbName()
				+ "." + w.getSourceTableName())).append(" ").append(w.getSourceTableAliasName());
		});
		// 添加其他from表
		workflow.getFromOrJoinList().stream().filter(w -> 1 == w.getIsEnable() && null == w.getIsPrimaryTable())
			.sorted(Comparator.comparingInt(ETLWorkflowTokenFromOrJoin::getId)).forEach(w -> {
			sb.append(" ").append(w.getJoinType()).append(" ").append(Optional.ofNullable(w.getSourceTableExpression()).orElse(w.getSourceDbName()
				+ "." + w.getSourceTableName())).append(" ").append(w.getSourceTableAliasName()).append(" on ")
				.append(Optional.ofNullable(w.getJoinOnExpression()).orElse(w.getSourceTableAliasName() + "."
					+ w.getJoinOnCurrentColumnName() + "=" + w.getJoinOnRightTableAliasName() + "." + w.getJoinOnRightTableColumnName()));
		});

		sb.append(" WHERE ").append(Optional.ofNullable(workflow.getFilter().getCommonFilterExpressionCustomized())
			.orElse(Optional.ofNullable(workflow.getFilter().getCommonFilterExpression()).orElse(" 1= 1 ")));
		if (primaryTableWhere.length() > 0) {
			sb.append(" AND ").append(primaryTableWhere);
		}
		return sb.toString();

	}
}
