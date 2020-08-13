package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLJob;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("jobMapper")
public interface DBETLJobMapper extends Mapper<ETLJob> {
    @Select("SELECT * FROM etl_job WHERE job_name=#{jobName}")
    ETLJob checkJobName(@Param("jobName") String jobName);

}

