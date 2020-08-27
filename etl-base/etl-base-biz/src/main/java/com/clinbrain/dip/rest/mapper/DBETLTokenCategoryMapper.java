package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLTokenCategory;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("tokenCategoryMapper")
public interface DBETLTokenCategoryMapper extends Mapper<ETLTokenCategory> {
    @Update("UPDATE etl_token_category SET  token_category_name=#{tokenCategoryName},updated_at=now() WHERE token_category_code=#{tokenCategoryCode}")
    int updateTokenCategory(ETLTokenCategory category);
}
