package com.clinbrain.dip.strategy.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.strategy.entity.JobVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (TTemplet)表数据库访问层
 *
 * @author lianglele
 * @since 2020-10-26 10:45:10
 */
@Mapper
public interface VersionMapper extends tk.mybatis.mapper.common.Mapper<JobVersion> {

	String selectViewSql(@Param("code") String code);

	void updateWorkflowCodeByVersionStatus(@Param("workflowCode") String workflowCode);


	IPage<JobVersion> selectVersionPage(Page page, @Param("workflowCode") String workflowCode);

}
