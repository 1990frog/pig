package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLConnection;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("connectionMapper")
public interface DBETLConnectionMapper extends Mapper<ETLConnection> {

    @Update("UPDATE etl_connection SET url=#{url},user=#{user},password=#{password},engine_id=#{engineId},updated_at=now()  WHERE connection_code=#{connectionCode}")
    boolean updateConnection(ETLConnection connection);
}
