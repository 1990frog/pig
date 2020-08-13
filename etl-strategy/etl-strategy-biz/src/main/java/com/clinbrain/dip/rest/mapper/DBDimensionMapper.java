package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinDimensionRowkey;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinModelDimension;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("DimensionMapper")
public interface DBDimensionMapper extends Mapper<KylinDimension> {

    public List<KylinDimension> selectDimsByModelId(@Param("modelId") Integer modelId);

    boolean insertModelDimension(@Param("modelDimensionId") Integer modelDimensionId, @Param("modelId") Integer modelId);

    void deleteModelDimensionByModelId(@Param("modelId") Integer modelId);

    List<KylinModelDimension> selectModelDimension(@Param("modelId") Integer modelId);

    List<KylinDimensionRowkey> selectDimensionAsRowkey(@Param("modelId") Integer modelId);

    boolean appendDimensionRowkey(KylinDimensionRowkey dimensionRowkey);

    void deleteDimensionRowkeyByModelId(@Param("modelId") Integer modelId);

    void deleteDimensionByTable(@Param("table") String table);

    List<KylinModel> selectModelDimensionByDimensionId(@Param("dimensionId") List<Integer> id);
}
