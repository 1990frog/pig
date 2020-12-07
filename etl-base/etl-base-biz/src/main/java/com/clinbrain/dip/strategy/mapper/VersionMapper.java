package com.clinbrain.dip.strategy.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinbrain.dip.strategy.entity.JobVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

	@Select("select * from t_workflow_sql_version where workflow_code = #{workflowCode} order by create_date desc limit 1")
	JobVersion selectLastVersion(@Param("workflowCode") String workflowCode);

	List<JobVersion> selectVersionPage(@Param("workflowCode") String workflowCode);

}
