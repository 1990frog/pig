package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.kylinmetadata.DataModelDesc2;
import com.clinbrain.dip.kylinmetadata.JoinTableDesc2;
import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinMeasure;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinModelDimension;
import com.clinbrain.dip.pojo.KylinModelLookup;
import com.clinbrain.dip.rest.mapper.DBDimensionMapper;
import com.clinbrain.dip.rest.mapper.DBModelsMapper;
import com.clinbrain.dip.rest.request.ModelRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.apache.kylin.metadata.model.JoinDesc;
import org.apache.kylin.metadata.model.ModelDimensionDesc;
import org.apache.kylin.metadata.model.PartitionDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2017/10/19.
 */
@Service
public class ModelService {

    @Autowired
    @Qualifier("ModelsMapper")
    private DBModelsMapper modelsMapper;

    @Autowired
    private DBDimensionMapper dimensionMapper;

    @Autowired
    private KylinAPIService kylin;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DimensionService dimensionService;

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    public KylinModel selectModelById(Integer id) {
        KylinModel model = modelsMapper.selectModelById(id);
        List<Integer> ids = dimensionMapper.selectModelDimension(model.getId()).stream().map(KylinModelDimension::getDimensionId).collect(Collectors.toList());
        Example example = new Example(KylinDimension.class);
        if (ids.size() > 0) {
            example.createCriteria().andIn("id", ids);
        }
        model.setDimensions(dimensionService.selectAllDimension(example, 0, 0));
        return model;
    }

    public List<KylinModel> selectOnlyModelByProjectId(Integer projectId) {
        Example example = new Example(KylinModel.class);
        example.createCriteria().andEqualTo("projectId", projectId);
        return modelsMapper.selectByExample(example);
    }

    /**
     * 查询model.by project_id 。
     * dimensions 需要单独查询再放进去，使用dimensionService中的查询方法
     *
     * @param projectId
     * @return
     */
    public List<KylinModel> selectModelByProjectId(Integer projectId) {
        List<KylinModel> models = modelsMapper.selectModelInfo(projectId);
        models.forEach(model -> {
            Example example = new Example(KylinDimension.class);
            List<Integer> ids = dimensionMapper.selectModelDimension(model.getId()).stream().map(KylinModelDimension::getDimensionId).collect(Collectors.toList());
            if (ids.size() > 0) {
                example.createCriteria().andIn("id", ids);
            }

            model.setDimensions(dimensionService.selectAllDimension(example, 0, 0));
        });

        return models;
    }

    @Transactional
    public void deleteModel(Integer id) {
        modelsMapper.deleteModelLookupByModelId(id);
        modelsMapper.deleteModelMeasureByModelId(id);
        dimensionMapper.deleteModelDimensionByModelId(id);
        dimensionMapper.deleteDimensionRowkeyByModelId(id);
        modelsMapper.deleteModel(id);
    }

    public boolean newCreateModel(KylinModel kylinModel) throws Exception {
        if (modelsMapper.newCreateModel(kylinModel)) {
            KylinModel modelMeta = this.selectModelByNames(kylinModel.getModelName());
            for (int i = 0, s = kylinModel.getLookupTables().size(); i < s; i++) {
                KylinModelLookup lookupTable = kylinModel.getLookupTables().get(i);
                String tableName = lookupTable.getTable().split("\\.")[1];
                for (int j = 0; j < lookupTable.getForeignKeyArray().length; j++) {
                    this.addLookupsTable(tableName, lookupTable.getJoinType(), lookupTable.getPrimaryKeyArray()[j], lookupTable.getForeignKeyArray()[j].toString(), modelMeta.getId());
                }
            }
            for (int i = 0, m = kylinModel.getMeasures().size(); i < m; i++) {
                KylinMeasure measureList = kylinModel.getMeasures().get(i);
                measureList.setModelId(modelMeta.getId());
                measureList.setMeasureName("");
                measureList.setMeasureExpression("");
                measureList.setMeasureType("");
                this.addModelMeasure(measureList);
            }
            this.insertDefaultMeasure(modelMeta.getId());
            Map<String, List> map = kylinModel.getDimensionsId();
            for (String key : map.keySet()) {
                for (int i = 0; i < map.get(key).size(); i++) {
                    String getValue = map.get(key).get(i).toString();
                    Integer id = new Integer(getValue);
                    dimensionService.insertModelDimension(id, modelMeta.getId());
                }
            }
            return true;
        }
        return false;
    }


    public void insertDefaultMeasure(int modelId) throws Exception {
        KylinMeasure defaultMeasureList = new KylinMeasure();
        defaultMeasureList.setMeasureColumn("1");
        defaultMeasureList.setMeasureColumnDatatype("integer");
        defaultMeasureList.setModelId(modelId);
        defaultMeasureList.setMeasureName("");
        defaultMeasureList.setMeasureExpression("");
        defaultMeasureList.setMeasureType("");
        this.addModelMeasure(defaultMeasureList);
    }

    public boolean addLookupsTable(String table, String joinType, String primary_key, String foreign_key, Integer id) throws Exception {
        if (!modelsMapper.addLookupsTable(table, joinType, primary_key, foreign_key, id)) {
            return true;
        } else {
            return false;
        }
    }

    public KylinModel selectModelByNames(String name) {
        return modelsMapper.selectModelByNames(name);
    }

    public boolean updateModelInfo(KylinModel kylinModel) throws Exception {
        KylinModel modelMeta = this.selectModelByNames(kylinModel.getModelName());
        if (modelsMapper.updateModelInfo(kylinModel)) {
            if (kylinModel.getLookupTables().size() > 0) {
                this.deleteModelLookupByModelId(modelMeta.getId());
                for (int i = 0, look = kylinModel.getLookupTables().size(); i < look; i++) {
                    KylinModelLookup lookupTable = kylinModel.getLookupTables().get(i);
                    String tableName = lookupTable.getTable().split("\\.")[1];
                    for (int j = 0; j < lookupTable.getForeignKeyArray().length; j++) {
                        this.addLookupsTable(tableName, lookupTable.getJoinType(), lookupTable.getPrimaryKeyArray()[j], lookupTable.getForeignKeyArray()[j], modelMeta.getId());
                    }
                }
            }
            if (kylinModel.getMeasures().size() > 0) {
                this.deleteModelMeasureByModelId(modelMeta.getId());
                for (int i = 0, m = kylinModel.getMeasures().size(); i < m; i++) {
                    KylinMeasure measureList = kylinModel.getMeasures().get(i);
                    measureList.setModelId(modelMeta.getId());
                    this.addModelMeasure(measureList);
                }
                this.insertDefaultMeasure(modelMeta.getId());
            }
            if (kylinModel.getDimensionsId().size() > 0) {
                this.deleteModelDimensionByModelId(modelMeta.getId());
                Map<String, List> map = kylinModel.getDimensionsId();
                for (String key : map.keySet()) {
                    for (int i = 0; i < map.get(key).size(); i++) {
                        String getValue = map.get(key).get(i).toString();
                        Integer id = new Integer(getValue);
                        dimensionService.insertModelDimension(id, modelMeta.getId());
                    }

                }
            }
            return true;
        }
        return false;
    }

    public boolean addModelMeasure(KylinMeasure measure) throws Exception {
        return modelsMapper.addModelMeasure(measure);
    }

    public void deleteModelMeasureByModelId(Integer id) {
        modelsMapper.deleteModelMeasureByModelId(id);
    }

    public void deleteModelLookupByModelId(Integer modelId) {
        modelsMapper.deleteModelLookupByModelId(modelId);
    }

    public void deleteModelDimensionByModelId(Integer id) {
        dimensionMapper.deleteModelDimensionByModelId(id);
    }

    public KylinCube selectKylinCubeByModelId(Integer modelId) {
        return modelsMapper.selectKylinCubeByModelId(modelId);
    }

    @Deprecated
    public List<KylinModel> getModels() {
        long dt1 = System.currentTimeMillis();
        //List<KylinModel> models2 = modelsMapper.selectAllModelsInfo();
        Long dt2 = System.currentTimeMillis();
        List<KylinModel> models = modelsMapper.selectModels();
        List<KylinModelLookup> lookups = modelsMapper.selectConcatLookups();
        List<KylinMeasure> measures = modelsMapper.selectMeasures();
        List<KylinDimension> dimensions = modelsMapper.selectDimensions();

        models.forEach(model -> {
            List<KylinModelLookup> modelLookups = Lists.newArrayList();
            List<KylinMeasure> modelMeasures = Lists.newArrayList();
            List<KylinDimension> modelDimensions = Lists.newArrayList();
            lookups.forEach(lookup -> {
                if (model.getId() == lookup.getModelId()) {
                    modelLookups.add(lookup);
                }
            });
            measures.forEach(measure -> {
                if (model.getId() == measure.getModelId()) {
                    modelMeasures.add(measure);
                }
            });
            dimensions.forEach(dimension -> {
                if (model.getId() == dimension.getModelId()) {
                    modelDimensions.add(dimension);
                }
            });
            model.setLookupTables(modelLookups);
            model.setDimensions(modelDimensions);
            model.setMeasures(modelMeasures);
        });
        Long dt3 = System.currentTimeMillis();
        logger.info((dt2 - dt1) + "|" + (dt3 - dt2));
        return models;
    }

    public ModelRequest createKylinModel(KylinModel model) throws Exception {
        ModelRequest request = new ModelRequest();
        String projectName = model.getProject().getProjectName();
        DataModelDesc2 modelDesc;
        try {
            modelDesc = transformModel(model);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        request.setModelDescData(jsonMapper.writeValueAsString(modelDesc));
        request.setUuid(UUID.randomUUID().toString());
        request.setModelName(model.getModelName());
        request.setSuccessful(true);
        request.setMessage("bean automation");
        request.setProject(projectName);

        return kylin.createModel(request);
    }

    private DataModelDesc2 transformModel(KylinModel model) {
        List<String> strList = Lists.newArrayList();
        Set<String> set = new HashSet<>();
        String schema = model.getSchema();
        DataModelDesc2 modelDesc = new DataModelDesc2();
        modelDesc.setUuid(UUID.randomUUID().toString());
        modelDesc.setName(model.getModelName());
        modelDesc.setRootFactTable(schema + "." + model.getFactTable());
        modelDesc.setFilterCondition(model.getFilterCondition());
        modelDesc.setCapacity(DataModelDesc.RealizationCapacity.valueOf(model.getCapacity()));
        // Set look up tables
        List<JoinTableDesc2> joinTableList = Lists.newArrayList();
        Optional.ofNullable(model.getLookupTables()).ifPresent(lookups -> lookups.forEach(item -> {
            JoinTableDesc2 joinTableDesc = new JoinTableDesc2();
            joinTableDesc.setTable(item.getTable());
            joinTableDesc.setKind(DataModelDesc.TableKind.valueOf("LOOKUP"));
            //joinDesc.setAlias();
            JoinDesc joinDesc = new JoinDesc();
            joinDesc.setType(item.getJoinType());
            String[] pkArray = new String[item.getPrimaryKeyArray().length];
            for (int i = 0; i < pkArray.length; i++) {
                String pk = item.getPrimaryKeyArray()[i];
                pk = item.getTable() + "." + pk;
                pkArray[i] = pk;
            }
            String[] fkArray = new String[item.getForeignKeyArray().length];
            for (int i = 0; i < fkArray.length; i++) {
                String fk = item.getForeignKeyArray()[i];
                fk = model.getFactTable() + "." + fk;
                fkArray[i] = fk;
            }
            joinDesc.setPrimaryKey(pkArray);
            joinDesc.setForeignKey(fkArray);
            joinTableDesc.setJoin(joinDesc);
            joinTableList.add(joinTableDesc);
        }));
        modelDesc.setJoinTables(joinTableList.toArray(new JoinTableDesc2[joinTableList.size()]));
        // Set dimensions, 查找所有的表的列添加到一起
        List<ModelDimensionDesc> dimensionList = Lists.newArrayList();
        Map<String, List<String>> dimensionTableMap = new HashMap<>();
        Optional.ofNullable(model.getDimensions()).ifPresent(dimList -> dimList.forEach(dim -> {
            List<String> columnList = dimensionTableMap.get(dim.getTable());
            if (columnList == null)
                columnList = Lists.newArrayList();
            if (dim.getDimensionColumns() != null) {
                List<String> tempList = dim.getDimensionColumns().stream().map(KylinDimension::getColumn).collect(Collectors.toList());
                columnList.addAll(tempList);
            } else {
                columnList.add(dim.getColumn());
            }
            dimensionTableMap.put(dim.getTable(), columnList);
        }));
        Optional.ofNullable(dimensionTableMap).ifPresent(map -> map.forEach((tableName, columnList) -> {
            ModelDimensionDesc dimensionDesc = new ModelDimensionDesc();
            dimensionDesc.setTable(tableName);
            dimensionDesc.setColumns(columnList.toArray(new String[columnList.size()]));
            dimensionList.add(dimensionDesc);
        }));
        modelDesc.setDimensions(dimensionList);
        // Set metrics
        Optional.ofNullable(model.getMeasures()).ifPresent(mea -> mea.forEach(measure -> {
            if (!"1".equalsIgnoreCase(measure.getMeasureColumn())) {
                strList.add(model.getFactTable() + "." + measure.getMeasureColumn());
            }
        }));
        modelDesc.setMetrics(strList.toArray(new String[strList.size()]));
        // Set partition
        PartitionDesc partitionDesc = new PartitionDesc();
        partitionDesc.setCubePartitionType(PartitionDesc.PartitionType.APPEND);
        partitionDesc.setPartitionDateColumn(model.getPartitionDateColumn());
        partitionDesc.setPartitionDateFormat(model.getPartitionDateFormat());
        modelDesc.setPartitionDesc(partitionDesc);
        return modelDesc;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean clone(Integer modelId, Integer projectId, String newModelName) throws Exception {

        KylinModel modelSelect = new KylinModel();
        modelSelect.setId(modelId);
        //1.查找model
        KylinModel kylinModel = modelsMapper.selectByPrimaryKey(modelSelect);
        if (kylinModel == null) {
            return false;
        }
        //2.设置model name为新的modelName 。id设置为null,用作新增
        kylinModel.setId(null);
        kylinModel.setModelName(newModelName);
        kylinModel.setCreatedAt(new Date());
        kylinModel.setUpdatedAt(new Date());
        kylinModel.setProjectId(projectId);
        modelsMapper.insert(kylinModel);

        Integer newModelId = kylinModel.getId();
        if (null == newModelId) {// 获取新插入的modelId用于字表的插入
            throw new RuntimeException("model克隆出错！");
        }
        modelsMapper.cloneModelDimension(modelId, newModelId);
        modelsMapper.cloneModelLookups(modelId, newModelId);
        modelsMapper.cloneModelMeasure(modelId, newModelId);
        return true;
    }
}
