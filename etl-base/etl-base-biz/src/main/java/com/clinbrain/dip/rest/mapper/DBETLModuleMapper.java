package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.*;
import com.clinbrain.dip.rest.vo.ModuleWorkflowStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2018/2/2.
 */
@Mapper
@Repository
public interface DBETLModuleMapper extends tk.mybatis.mapper.common.Mapper<ETLModule> {
    List<ETLJob> getJobs(@Param("topicId") Integer topicId, @Param("jobName") String jobName);

    int selectModuleByTopicId(@Param("topicId") Integer topicId,@Param("jobId") Integer jobId);

    ETLModule selectModuleDetailByCode(@Param("moduleCode") String moduleCode);

    List<ETLModule> selectModuleDetails(@Param("topicId") Integer topicId, @Param("jobId") Integer jobId, @Param("hospital") String hospital, @Param("moduleName") String moduleName);

    List<ETLModule> selectAllModules(@Param("topicId") Integer topicId, @Param("jobId") Integer jobId, @Param("hospital") String hospital, @Param("moduleName") String moduleName);

    void renovateModuleStatus(@Param("code") String code, @Param("enabled") Integer enabled);

    int checkTopicIdByJob(@Param("topicId") Integer topicId);

    int checkConnectionByModule(@Param("connectionCode") String connectionCode);

    int checkTokenByWorkflowToken(@Param("code") String code);

    ETLLogSummary getLogsInfoByModule(@Param("module") String module, @Param("runtime") String runtime);

    List<ETLLogDetail> getLogDetailBySummaryId(@Param("summary_id") Integer summaryId);

    List<ETLHospital> queryHospitals();

    int updateHospital(ETLHospital hospital);

    int insertHospital(ETLHospital hospital);

    void deleteHospital(@Param("hospitalCode") String hospitalCode);

    int updateModuleByCode(ETLModule module);

    List<Map> selectModuleCodeByWorkflowInfo();

    List<ModuleWorkflowStatus> selectWorkflowStatus(@Param("moduleCode") String moduleCode, @Param("uuid") String uuid);


	ETLConnection selectTargetConnection(@Param("workflowCode") String workflowCode);

	@Select("select a.* from etl_module a left join etl_job_module b on a.module_code = b.module_code where b.job_id = #{jobId}")
	List<ETLModule> selectModulesByJobId(@Param("jobId") Integer jobId);

	@Update("update etl_module set dependency_code = #{dependencyCode} where dependency_code = #{moduleCode}")
	int updateDependencyByModuleCode(@Param("moduleCode") String moduleCode, @Param("dependencyCode") String dependencyCode);
}
