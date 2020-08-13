package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.EtlEmpid;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("EmpidMapper")
public interface DBETLEmpidMapper extends Mapper<EtlEmpid> {

    int updateStatusBatch(@Param("list") List<String> list);

    int batchInsert(@Param("list") List<EtlEmpid> list);
}
