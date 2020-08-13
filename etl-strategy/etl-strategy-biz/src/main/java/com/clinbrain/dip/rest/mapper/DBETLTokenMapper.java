package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLToken;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
@Repository("tokenMapper")
public interface DBETLTokenMapper extends Mapper<ETLToken> {
        @Update("UPDATE etl_token SET token_name=#{tokenName},token_category_code=#{tokenCategoryCode} ,updated_at=now() WHERE token_code=#{tokenCode}")
        boolean updateToken(ETLToken token);
}
