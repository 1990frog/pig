package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLWorkflowToken;
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
public interface DBETLWorkflowTokenMapper extends tk.mybatis.mapper.common.Mapper<ETLWorkflowToken> {

    @Update("update etl_workflow_token set is_enable = #{enable} where workflow_code = #{workflowCode}")
    void updateEnableStatusByWorkflowCode(@Param("enable") int enable, @Param("workflowCode") String workflowCode);

    @Delete("delete from etl_workflow_token WHERE workflow_code in (select workflow_code from etl_workflow WHERE module_code=#{moduleCode})")
    void removeEtlWorkflowTokenByModuleCode(@Param("moduleCode") String moduleCode);
}
