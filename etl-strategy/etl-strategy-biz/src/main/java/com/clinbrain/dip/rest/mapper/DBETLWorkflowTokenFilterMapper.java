package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLWorkflowTokenFilter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by Liaopan on 2018/2/2.
 */
@Repository
@Mapper
public interface DBETLWorkflowTokenFilterMapper extends tk.mybatis.mapper.common.Mapper<ETLWorkflowTokenFilter> {
    @Delete("delete from etl_workflow_token_filter WHERE workflow_token_code in (select workflow_token_code from etl_workflow_token WHERE workflow_code in (select workflow_code from etl_workflow WHERE module_code=#{moduleCode}))")
    void removeWorkflowTokenFilter(@Param("moduleCode") String moduleCode);
}
