package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.pojo.ETLWorkflow;
import com.clinbrain.dip.pojo.ETLWorkflowSelectRegex;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.strategy.mapper.EtlWorkflowSelectRegexMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (EtlWorkflowSelectRegex)表服务接口
 *
 * @author Liaopan
 * @since 2021-01-12 16:15:21
 */
@Service
public class EtlWorkflowSelectRegexService extends BaseService<ETLWorkflowSelectRegex> {
	@Autowired
	private EtlWorkflowSelectRegexMapper etlWorkflowSelectRegexMapper;

	public int deleteByWorkflowCode(List<ETLWorkflow> workflowCodes) {

		if(CollectionUtils.isNotEmpty(workflowCodes)) {
			final List<String> workflowCodeList = workflowCodes.stream().map(ETLWorkflow::getWorkflowCode).collect(Collectors.toList());
			final Weekend<ETLWorkflowSelectRegex> weekend = Weekend.of(ETLWorkflowSelectRegex.class);
			weekend.weekendCriteria().andIn(ETLWorkflowSelectRegex::getWorkflowCode, workflowCodeList);
			return etlWorkflowSelectRegexMapper.deleteByExample(weekend);
		}
		return 0;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveAll(List<ETLWorkflowSelectRegex> regexes) {
		if(CollectionUtils.isNotEmpty(regexes)) {
			regexes.forEach(s -> {
				etlWorkflowSelectRegexMapper.insert(s);
			});
		}
	}
}
