package com.clinbrain.dip.strategy.mapper;

import com.clinbrain.dip.strategy.entity.HospitalSystem;
import org.apache.ibatis.annotations.Mapper;

/**
 * (THospitalSystem)表数据库访问层
 *
 * @author Liaopan
 * @since 2020-10-30 16:04:25
 */
@Mapper
public interface THospitalSystemMapper extends tk.mybatis.mapper.common.Mapper<HospitalSystem> {
}
