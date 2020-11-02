package com.clinbrain.dip.strategy.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.strategy.entity.JobVersion;
import com.clinbrain.dip.strategy.mapper.VersionMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * (TTemplet)表服务实现类
 *
 * @author Liaopan
 * @since 2020-09-04 10:48:37
 */
@Service
@AllArgsConstructor
public class VersionService extends BaseService<JobVersion> {

	private final VersionMapper mapper;

	public IPage selectVersionList(Page page, String workCode){
		return mapper.selectVersionPage(page,workCode);
	}
}
