package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.EtlEmpid;
import com.clinbrain.dip.pojo.EtlModuleTemplate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("etlModuleTemplateMapper")
public interface DBETLModuleTemplateMapper extends Mapper<EtlModuleTemplate> {

}
