package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLWorkflow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by Liaopan on 2018/2/2.
 */
@Repository
@Mapper
public interface DBETLWorkflowMapper extends tk.mybatis.mapper.common.Mapper<ETLWorkflow> {

    @Delete("delete from etl_workflow WHERE module_code=#{moduleCode}")
    void removeEtlWorkflowByModuleCode(@Param("moduleCode") String moduleCode);
}
