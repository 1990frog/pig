package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinMeasure;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinModelLookup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("ModelsMapper")
public interface DBModelsMapper extends Mapper<KylinModel> {

    KylinModel selectModelById(@Param("id") Integer id);

    void deleteModel(@Param("modelId") Integer id);

    List<KylinModel> selectModelInfo(@Param("proId") Integer proId);

    void deleteModelLookupByModelId(@Param("modelId") Integer id);

    void deleteModelMeasureByModelId(@Param("modelId") Integer id);

    boolean newCreateModel(KylinModel kylinModel);

    boolean addLookupsTable(@Param("table") String table, @Param("joinType") String joinType, @Param("primary_key") String primary_key, @Param("foreign_key") String foreign_key, @Param("modelId") Integer Id);

    KylinModel selectModelByNames(@Param("name") String name);

    boolean updateModelInfo(KylinModel createModelJson);

    boolean addModelMeasure(KylinMeasure kylinMeasure);

    List<KylinModel> selectModels();

    List<KylinModelLookup> selectConcatLookups();

    List<KylinMeasure> selectMeasures();

    List<KylinDimension> selectDimensions();

    boolean cloneModelDimension(@Param("modelId") Integer modelId, @Param("newModelId") Integer newModelId);

    boolean cloneModelMeasure(@Param("modelId") Integer modelId, @Param("newModelId") Integer newModelId);

    boolean cloneModelLookups(@Param("modelId") Integer modelId, @Param("newModelId") Integer newModelId);

    KylinCube selectKylinCubeByModelId(@Param("modelId") Integer modelId);

    List<String> selectProjectRelatedTables(@Param("projectName") String projectName);

    List<Map> selectProjectRelatedDimensions(@Param("projectName") String projectName);

    List<Map> selectProjectRelatedAllDimensionAttributes(@Param("projectName") String projectName);

    List<Map> selectProjectRelatedDimensionAttributes(@Param("projectName") String projectName, @Param("tableName") String tableName);

    List<String> selectProjectRelatedDimensionHierarchy(@Param("projectName") String projectName);

    List<Map> selectProjectRelatedMGs(@Param("projectName") String projectName);

    List<Map> selectProjectRelatedMeasures(@Param("projectName") String projectName, @Param("mgName") String mgName);

    List<Map> selectProjectRelatedFL(@Param("projectName") String projectName, @Param("mgName") String mgName);

    List<Map> selectProjectRelatedCalMeasures(@Param("projectName") String projectName);
}
