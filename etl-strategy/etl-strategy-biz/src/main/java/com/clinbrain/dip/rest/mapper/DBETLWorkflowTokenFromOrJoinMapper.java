package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLWorkflowTokenFromOrJoin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * Created by Liaopan on 2018/2/2.
 */
@Repository
@Mapper
public interface DBETLWorkflowTokenFromOrJoinMapper extends tk.mybatis.mapper.common.Mapper<ETLWorkflowTokenFromOrJoin> {

    @Update("update etl_workflow_token_from_or_join set is_enable = #{enable} where workflow_token_code = #{workflowTokenCode}")
    void updateEnableStatusByCode(@Param("enable") int enable, @Param("workflowTokenCode") String workflowTokenCode);

    @Delete("delete from etl_workflow_token_from_or_join  WHERE workflow_token_code in (select workflow_token_code from etl_workflow_token WHERE workflow_code in (select workflow_code from etl_workflow WHERE module_code=#{moduleCode}))")
    void removeWorkflowTokenFromByModuleCode(@Param("moduleCode") String moduleCode);
}
