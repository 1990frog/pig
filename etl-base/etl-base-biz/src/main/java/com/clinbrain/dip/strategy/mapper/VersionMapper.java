package com.clinbrain.dip.strategy.mapper;

import com.clinbrain.dip.strategy.entity.JobVersion;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * (TTemplet)表数据库访问层
 *
 * @author lianglele
 * @since 2020-10-26 10:45:10
 */
@org.apache.ibatis.annotations.Mapper
public interface VersionMapper extends Mapper<JobVersion> {

	String selectViewSql(@Param("code") String code);

	void updateWorkflowCodeByVersionStatus(@Param("workflowCode") String workflowCode);

}
