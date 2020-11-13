package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.pojo.ETLModule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("jobMapper")
public interface DBETLJobMapper extends Mapper<ETLJob> {
    @Select("SELECT * FROM etl_job WHERE job_name=#{jobName}")
    ETLJob checkJobName(@Param("jobName") String jobName);

    @Select("select * from etl_module a inner join etl_job_module b on a.module_code = b.module_code and a.enabled = 1 and b.job_id = #{jobId}")
	List<ETLModule> selectModulesByJobId(@Param("jobId") Integer jobId);

}

