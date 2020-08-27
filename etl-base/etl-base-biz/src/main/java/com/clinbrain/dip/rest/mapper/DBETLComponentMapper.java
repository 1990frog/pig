package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLComponent;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("componentMapper")
public interface DBETLComponentMapper extends Mapper<ETLComponent> {
  @Update("UPDATE etl_component SET component_code=#{componentCode} ,component_name=#{componentName},component_class=#{componentClass},component_category=#{componentCategory},component_desc=#{componentDesc},param_define=#{paramDefine},updated_at=now() WHERE id=#{id}")
  boolean updateComponent(ETLComponent component);
}
