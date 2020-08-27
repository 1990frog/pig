package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLEngine;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("engineMapper")
public interface DBETLEngineMapper extends Mapper<ETLEngine> {
    @Update("UPDATE etl_engine SET engine_name=#{engineName},engine_class=#{engineClass},description=#{description},created_at=now() WHERE id=#{id}")
    boolean updateEngine(ETLEngine engine);
}
