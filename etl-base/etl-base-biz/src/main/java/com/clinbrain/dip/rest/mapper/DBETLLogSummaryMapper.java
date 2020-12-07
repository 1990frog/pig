package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLLogSummary;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2018/3/2.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("logSummaryMapper")
public interface DBETLLogSummaryMapper extends Mapper<ETLLogSummary> {

    @Update("UPDATE etl_logsummary SET end_date=#{end_date} WHERE summary_id=#{summary_id}")
    public void updateLogsummaryEndDateBySummaryId(@Param("end_date") Date endDate, @Param("summary_id") Long summaryId);

    public List<ETLLogSummary> selectLogSummaryByJobId(@Param("jobId") Integer jobId, @Param("moduleCode") String moduleCode, @Param("status") Integer status, @Param("hospital") String hospital);

    public List<Map> selectJobModuleByJobId(@Param("jobId") Integer jobId);

    @Update("UPDATE etl_logsummary SET status = 0 WHERE summary_id = #{summaryId}")
    void updateLogsummaryStatusBySummaryId(@Param("summaryId") Integer summaryId);

    @Update("UPDATE etl_logdetail SET status=0 WHERE detail_id=#{detailId}")
    void updateLogDetailByUuid(@Param("detailId") Integer detailId);

    @Select("select * from etl_logsummary where batch_id = #{batchId}")
    ETLLogSummary selectLogSummaryByBatchId(@Param("batchId") String batchId);
}

