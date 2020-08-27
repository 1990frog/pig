package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinMeasure;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("MeasureMapper")
public interface DBMeasureMapper extends Mapper<KylinMeasure> {

    /**
     * 清除modelmeasure表中除了column列的其他数据，cube修改时使用
     * @return
     */
    int clearMeasureNameByModelId(Integer modelId);
}
