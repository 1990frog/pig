package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.pojo.EtlCommonExpression;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.strategy.mapper.EtlCommonExpressionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.Collection;

/**
 * (EtlCommonExpression)表服务接口
 *
 * @author Liaopan
 * @since 2021-01-11 17:54:06
 */
@Service
public class EtlCommonExpressionService extends BaseService<EtlCommonExpression> {
	@Autowired
	private EtlCommonExpressionMapper etlCommonExpressionMapper;

	public int deleteByIds(Collection<Long> ids) {
		final WeekendCriteria<EtlCommonExpression, Object> example = Weekend.of(EtlCommonExpression.class).weekendCriteria()
			.andIn(EtlCommonExpression::getId, ids);
		return etlCommonExpressionMapper.deleteByExample(example);
	}
}
