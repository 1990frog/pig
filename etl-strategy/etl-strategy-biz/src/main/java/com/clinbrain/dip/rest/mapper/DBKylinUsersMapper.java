package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinUsers;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2018/1/30.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("kylinUsersMapper")
public interface DBKylinUsersMapper extends Mapper<KylinUsers> {
}
