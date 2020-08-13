package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLDataxflow;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("dataxflowMapper")
public interface DBETLDataxflowMapper extends Mapper<ETLDataxflow> {

}
