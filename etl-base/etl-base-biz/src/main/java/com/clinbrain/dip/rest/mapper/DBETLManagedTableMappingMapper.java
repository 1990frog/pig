package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.EtlHistablePartitionsConfiguration;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository
public interface DBETLManagedTableMappingMapper extends Mapper<EtlHistablePartitionsConfiguration> {

}
