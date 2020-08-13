package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLWorkflowDataxflow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by Liaopan on 2018/2/2.
 */
@Repository
@Mapper
public interface DBETLWorkflowDataxflowMapper extends tk.mybatis.mapper.common.Mapper<ETLWorkflowDataxflow> {
    @Delete("delete from etl_workflow_dataxflow WHERE workflow_code in (select workflow_code from etl_workflow WHERE module_code=#{moduleCode})")
    void removeWorkflowDataXFlow(@Param("moduleCode") String moduleCode);

}
