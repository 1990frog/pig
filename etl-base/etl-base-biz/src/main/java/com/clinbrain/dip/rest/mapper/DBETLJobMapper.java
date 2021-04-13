package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.rest.vo.ModuleCheckStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("jobMapper")
public interface DBETLJobMapper extends Mapper<ETLJob> {
    @Select("SELECT * FROM etl_job WHERE job_name=#{jobName} limit 1")
    ETLJob checkJobName(@Param("jobName") String jobName);

	@Select("SELECT * FROM etl_job WHERE job_name=#{jobName} and topic_id = #{topicId} limit 1")
	ETLJob checkJobNameUnderTopicId(@Param("jobName") String jobName, @Param("topicId") Integer topicId);

    @Select("select * from etl_module a inner join etl_job_module b on a.module_code = b.module_code and a.enabled = 1 and b.job_id = #{jobId}")
	List<ETLModule> selectModulesByJobId(@Param("jobId") Integer jobId);

	List<ETLModule> selectModulesByJobIdAndCode(@Param("jobId") Integer jobId, @Param("moduleCodes") List<String> moduleCodes);

    @Select("select module_code, count(check_id) as count from etl_job_module a left join etl_logsummary b " +
		"on a.module_code = b.module_name where a.job_id = #{jobId} group by module_name")
    List<ModuleCheckStatus> selectModuleCheckStatusByJobId(@Param("jobId") Integer jobId);
}

