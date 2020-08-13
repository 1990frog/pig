package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.EtlJobModule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("jobModuleMapper")
public interface DBETLJobModuleMapper extends Mapper<EtlJobModule> {

    @Update("update etl_job_module set job_id = #{jobId} where module_code = #{moduleCode}")
    boolean updateJobIdByModuleCode(@Param("jobId") Integer jobId, @Param("moduleCode") String moduleCode);

    @Delete("delete from etl_job_module WHERE module_code=#{moduleCode}")
    void removeETLJobModuleByModuleCode(@Param("moduleCode") String moduleCode);
}
