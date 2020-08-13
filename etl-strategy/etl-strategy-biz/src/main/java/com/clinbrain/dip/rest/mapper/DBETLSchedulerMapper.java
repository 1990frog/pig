package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLJobScheduler;
import com.clinbrain.dip.pojo.ETLScheduler;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("schedulerMapper")
public interface DBETLSchedulerMapper extends Mapper<ETLScheduler> {


    @Insert("insert into etl_job_scheduler(job_id,scheduler_id) values(#{jobId},#{schedulerId})")
    int saveJobScheduler(ETLJobScheduler jobScheduler);

    @Delete("delete from etl_job_scheduler where job_id = #{jobId}")
    int deleteJobSchedulerByJobId(@Param("jobId") int jobId);


}

