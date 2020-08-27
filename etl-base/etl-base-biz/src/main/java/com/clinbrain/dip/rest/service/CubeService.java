package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.kylinmetadata.CubeDesc2;
import com.clinbrain.dip.metadata.DictionaryDesc2;
import com.clinbrain.dip.metadata.KylinCubeSegment;
import com.clinbrain.dip.metadata.ParameterDesc2;
import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinCubeAggGroup;
import com.clinbrain.dip.pojo.KylinCubeDictionary;
import com.clinbrain.dip.pojo.KylinCubeProperty;
import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinDimensionRowkey;
import com.clinbrain.dip.pojo.KylinMeasure;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinModelDimension;
import com.clinbrain.dip.rest.bean.PropertyBean;
import com.clinbrain.dip.rest.mapper.DBCubeAggGroupMapper;
import com.clinbrain.dip.rest.mapper.DBCubeDictionaryMapper;
import com.clinbrain.dip.rest.mapper.DBCubeMapper;
import com.clinbrain.dip.rest.mapper.DBCubePropertyMapper;
import com.clinbrain.dip.rest.mapper.DBDimensionMapper;
import com.clinbrain.dip.rest.mapper.DBDimensionRowkeyMapper;
import com.clinbrain.dip.rest.mapper.DBMeasureMapper;
import com.clinbrain.dip.rest.request.CubeRequest;
import com.clinbrain.dip.rest.request.SegmentRequest;
import com.clinbrain.dip.rest.response.CubeResponse;
import com.clinbrain.dip.rest.response.ResponseData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.cube.model.AggregationGroup;
import org.apache.kylin.cube.model.DimensionDesc;
import org.apache.kylin.cube.model.HBaseColumnDesc;
import org.apache.kylin.cube.model.HBaseColumnFamilyDesc;
import org.apache.kylin.cube.model.HBaseMappingDesc;
import org.apache.kylin.cube.model.RowKeyColDesc;
import org.apache.kylin.cube.model.RowKeyDesc;
import org.apache.kylin.cube.model.SelectRule;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.apache.kylin.metadata.model.FunctionDesc;
import org.apache.kylin.metadata.model.MeasureDesc;
import org.apache.kylin.metadata.model.ParameterDesc;
import org.apache.kylin.metadata.project.ProjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2017/10/19.
 */
@Service
@Log4j
public class CubeService {

    @Autowired
    @Qualifier("CubesMapper")
    private DBCubeMapper cubeMapper;

    @Autowired
    @Qualifier("CubeAggGroupMapper")
    private DBCubeAggGroupMapper aggGroupMapper;

    @Autowired
    @Qualifier("CubeDictionaryMapper")
    private DBCubeDictionaryMapper dictionaryMapper;

    @Autowired
    @Qualifier("CubePropertyMapper")
    private DBCubePropertyMapper propertyMapper;

    @Autowired
    @Qualifier("DimensionMapper")
    private DBDimensionMapper dimensionMapper;

    @Autowired
    @Qualifier("MeasureMapper")
    private DBMeasureMapper measureMapper;

    @Autowired
    @Qualifier("DimensionRowkeyMapper")
    private DBDimensionRowkeyMapper rowkeyMapper;

    @Autowired
    private ModelService modelService;

    @Autowired
    private DimensionService dimensionService;

    @Autowired
    private KylinAPIService kylin;

    @Autowired
    private PropertyBean propertyBean;

    private final Logger logger = LoggerFactory.getLogger(CubeService.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Cacheable(cacheNames = "cubes")
    public ResponseData.Page<KylinCube> selectAllCubes(Integer projectId, String searchText, String orderBy, Integer offset, Integer limit) {

        long count = cubeMapper.selectCustomCount(projectId, searchText);
        List<KylinCube> cubes = cubeMapper.selectAllCubes(projectId, searchText, orderBy, offset, limit);
        return new ResponseData.Page<KylinCube>(count, cubes);
    }


    @Cacheable(cacheNames = "cubes", key = "'cueb_'+#cubeId")
    public CubeResponse selectOne(Integer cubeId) {
        KylinCube kylinCube = cubeMapper.selectOneByPrimaryKey(cubeId);

        kylinCube.setModelName(kylinCube.getModel().getModelName());
        kylinCube.setProjectName(kylinCube.getProject().getProjectName());

        List<Integer> ids = dimensionMapper.selectModelDimension(kylinCube.getModelId())
                .stream().map(KylinModelDimension::getDimensionId).collect(Collectors.toList());
        Example example = new Example(KylinDimension.class);
        example.createCriteria().andIn("id", ids);
        kylinCube.getModel().setDimensions(dimensionService.selectAllDimension(example, 0, 0));


        CubeResponse cubeResponse = new CubeResponse();
        cubeResponse.setCubeDesc(transformCube(kylinCube));
        cubeResponse.setModel(kylinCube.getModel());
        cubeResponse.setProject(kylinCube.getProject());
        return cubeResponse;
    }

    public KylinCube selectCubeById(Integer cubeId) {
        KylinCube cube = cubeMapper.selectOneByPrimaryKey(cubeId);
        List<Integer> ids = dimensionMapper.selectModelDimension(cube.getModelId())
                .stream().map(KylinModelDimension::getDimensionId).collect(Collectors.toList());
        Example example = new Example(KylinDimension.class);
        example.createCriteria().andIn("id", ids);
        cube.getModel().setDimensions(dimensionService.selectAllDimension(example, 0, 0));
        return cube;
    }

    /**
     * 删除cube,及其字表数据
     *
     * @param id
     * @return
     */
    @CacheEvict(cacheNames = "cubes", allEntries = true)
    @Transactional
    public Boolean dropCubeById(Integer id) {

        //删除agggroup
        Example cubeAggGroupExample = new Example(KylinCubeAggGroup.class);
        cubeAggGroupExample.createCriteria().andEqualTo("cubeId", id);
        aggGroupMapper.deleteByExample(cubeAggGroupExample);

        // 删除dictionary
        Example cubeDictionaryExample = new Example(KylinCubeDictionary.class);
        cubeDictionaryExample.createCriteria().andEqualTo("cubeId", id);
        dictionaryMapper.deleteByExample(cubeDictionaryExample);

        //删除property
        Example cubePropertyExample = new Example(KylinCubeProperty.class);
        cubePropertyExample.createCriteria().andEqualTo("cubeId", id);
        dictionaryMapper.deleteByExample(cubePropertyExample);

        KylinCube cube = new KylinCube();
        cube.setId(id);
        return cubeMapper.deleteByPrimaryKey(cube) > 0;
    }

    /**
     * 新增cube, 需要添加相关字表内容
     *
     * @param cubeDesc
     */
    @CacheEvict(cacheNames = "cubes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpadte(CubeDesc2 cubeDesc) throws Exception {

        KylinCube cubeMeta = transformVO2DBCube(cubeDesc);

        // create
        if (null == cubeMeta.getId()) {
            cubeMapper.insert(cubeMeta);
            Integer cubeId = cubeMeta.getId();
            if (cubeId == null || cubeId == 0) {
                throw new RuntimeException("保存Cube内容出错，获取不到主键ID的值！");
            }
            // add agggroup
            cubeMeta.getCubeAggGroupList().stream().peek(cubeAggGroup -> cubeAggGroup.setCubeId(cubeId))
                    .forEach(aggGroup -> aggGroupMapper.insert(aggGroup));

            // add dictionary
            cubeMeta.getCubeDictionaryList().stream().peek(cubeDictionary -> cubeDictionary.setCubeId(cubeId))
                    .forEach(cubeDictionary -> dictionaryMapper.insert(cubeDictionary));

            // add property
            cubeMeta.getCubePropertyList().stream().peek(cubeProperty -> cubeProperty.setCubeId(cubeId))
                    .forEach(cubeProperty -> propertyMapper.insert(cubeProperty));

            //update measure
            updateMeasures(cubeMeta);
            updateDimensionRowkey(cubeMeta);
        } else { // update

            // 删除所有- 再增加
            editCubeAggGroup(cubeMeta);
            editCubeDictionary(cubeMeta);
            editCubeProperty(cubeMeta);
            //update measure
            updateMeasures(cubeMeta);
            updateDimensionRowkey(cubeMeta);
            cubeMapper.updateByPrimaryKeySelective(cubeMeta);
        }
    }

    /**
     * 更新measure
     *
     * @param cube
     */
    private void updateMeasures(KylinCube cube) {
        //1. 清除原有的measure的值

        measureMapper.clearMeasureNameByModelId(cube.getModelId());

        // 2.通过modelid,measure_columnName 修改对应的记录
        List<KylinMeasure> kylinMeasureList = cube.getModel().getMeasures();
        if (!kylinMeasureList.isEmpty()) {
            kylinMeasureList.forEach(measure -> {
                if (StringUtils.isNotBlank(measure.getMeasureName())) {
                    Example example = new Example(KylinMeasure.class);
                    example.createCriteria().andEqualTo("modelId", cube.getModelId())
                            .andEqualTo("measureColumn", measure.getMeasureColumn());

                    measure.setUpdatedAt(new Date());
                    measureMapper.updateByExampleSelective(measure, example);
                }
            });
        }
    }

    // 更新rowkey数据
    private void updateDimensionRowkey(KylinCube cube) {
        // 根据model_id 和 rowkey_column 修改对应的sort值
        List<KylinDimensionRowkey> kylinRowkeyList = cube.getModel().getRowkeys();
        if (!kylinRowkeyList.isEmpty()) {
            int count = 1;
            for (KylinDimensionRowkey rowkey : kylinRowkeyList) {
                Example example = new Example(KylinDimensionRowkey.class);
                example.createCriteria().andEqualTo("modelId", cube.getModelId())
                        .andEqualTo("rowkeyColumn", rowkey.getRowkeyColumn());
                rowkey.setRowkeySort(String.valueOf(count++));
                rowkeyMapper.updateByExampleSelective(rowkey, example);
            }
        }
    }

    private void editCubeAggGroup(KylinCube cubeMeta) {
        Integer cubeId = cubeMeta.getId();
        // 1，删除cube对应的所有数据
        Example example = new Example(KylinCubeAggGroup.class);
        example.createCriteria().andEqualTo("cubeId", cubeId);
        aggGroupMapper.deleteByExample(example); //删除cubeId中id不存在的数据
        //2, 重新添加数据
        cubeMeta.getCubeAggGroupList().stream().peek(cubeAggGroup -> cubeAggGroup.setCubeId(cubeId))
                .forEach(aggGroup -> aggGroupMapper.insert(aggGroup));
    }

    private void editCubeDictionary(KylinCube cubeMeta) {
        Integer cubeId = cubeMeta.getId();
        // 1，删除cube对应的所有数据
        Example example = new Example(KylinCubeDictionary.class);
        example.createCriteria().andEqualTo("cubeId", cubeId);
        dictionaryMapper.deleteByExample(example);
        //2, 重新添加所有数据
        cubeMeta.getCubeDictionaryList().stream().peek(cubeDictionary -> cubeDictionary.setCubeId(cubeId))
                .forEach(cubeDictionary -> dictionaryMapper.insert(cubeDictionary));
    }

    private void editCubeProperty(KylinCube cubeMeta) {
        Integer cubeId = cubeMeta.getId();
        // 1，删除cube对应的所有数据
        Example example = new Example(KylinCubeProperty.class);
        example.createCriteria().andEqualTo("cubeId", cubeId);
        propertyMapper.deleteByExample(example);
        //2, 重新添加所有数据
        cubeMeta.getCubePropertyList().stream().peek(cubeProperty -> cubeProperty.setCubeId(cubeId))
                .forEach(cubeProperty -> propertyMapper.insert(cubeProperty));
    }


    /**
     * 使用selectCubeById，或者selectAllCubes(null,...)代替查询
     *
     * @return
     */
    @Deprecated
    public List<KylinCube> getCubes() {
        List<KylinModel> models = modelService.getModels();
        List<KylinCube> cubes = cubeMapper.selectCubes();
        List<KylinCubeAggGroup> cubeAggGroups = cubeMapper.selectCubeAggGroups();
        List<KylinCubeDictionary> cubeDictionaries = cubeMapper.selectCubeDictionaries();
        List<KylinCubeProperty> cubeProperties = cubeMapper.selectCubeProperties();
        List<KylinDimension> cubeDimensions = cubeMapper.selectCubeDimensions();

        for (KylinCube cube : cubes) {
            for (KylinModel model : models) {
                if (cube.getModelId() == model.getId()) {
                    cube.setModel(model);
                    break;
                }
            }
        }
        cubes.forEach(cube -> {
            List<KylinCubeAggGroup> aggGroupList = Lists.newArrayList();
            List<KylinCubeDictionary> dictionaryList = Lists.newArrayList();
            List<KylinCubeProperty> propertyList = Lists.newArrayList();
            List<KylinDimension> dimensionList = Lists.newArrayList();
            cubeAggGroups.forEach(agg -> {
                if (cube.getId() == agg.getCubeId()) {
                    aggGroupList.add(agg);
                }
            });
            cubeDictionaries.forEach(dic -> {
                if (cube.getId() == dic.getCubeId()) {
                    dictionaryList.add(dic);
                }
            });
            cubeProperties.forEach(prop -> {
                if (cube.getId() == prop.getCubeId()) {
                    propertyList.add(prop);
                }
            });
            cubeDimensions.forEach(dim -> {
                if (cube.getModelId() == dim.getModelId()) {
                    dimensionList.add(dim);
                }
            });
            cube.setCubeAggGroupList(aggGroupList);
            cube.setCubeDictionaryList(dictionaryList);
            cube.setCubePropertyList(propertyList);
            cube.setCubeDimensionList(dimensionList);
        });
        return cubes;
    }

    /**
     * 创建cube时判断kylin上是否存在对应的项目
     *
     * @param projectName
     * @return
     * @throws Exception
     */
    public boolean checkProjectExist(String projectName) throws Exception {

        ProjectInstance projectInstance = kylin.getProjectByName(projectName);
        return projectInstance != null && !StringUtils.isEmpty(projectInstance.getName());
    }

    /**
     * 在远程kylin上部署cube
     *
     * @param projectName 项目
     * @param cubes       cube数据
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> createKylinCubes(String projectName, List<KylinCube> cubes) throws Exception {
        List<Map<String, String>> ret = Lists.newArrayList();
        CubeInstance cubeInstance;
        boolean hasError = false;
        for (final KylinCube cube : cubes) {
            Map<String, String> resultMap = Maps.newLinkedHashMap();
            StringBuilder stringBuilder = new StringBuilder();
            String cubeName = cube.getCubeName();
            String modelName = cube.getModel().getModelName();
            resultMap.put("cubeName", cubeName);
            try {
                cubeInstance = kylin.getCubeByName(projectName, cubeName);
                if (cubeInstance != null && cubeInstance.isReady()) {
                    stringBuilder.append("cube正在运行, 无法创建");
                    resultMap.put("status", "false");
                    resultMap.put("message", stringBuilder.toString());
                    ret.add(resultMap);
                    hasError = true;
                    continue;
                }
                DataModelDesc modelDesc = kylin.getModeByName(projectName, modelName);
                if (cubeInstance != null) {
                    kylin.deleteCube(cubeName);
                    stringBuilder.append("已存在，删除该cube。");
                }
                if (modelDesc != null) {
                    kylin.deleteModel(modelName);
                    stringBuilder.append(modelName).append(" 已存在，先删除。");
                }

                modelService.createKylinModel(cube.getModel());
                createKylinCube(cube);
                stringBuilder.append("创建成功！");
                //创建cube成功后开始build cube
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 判断是否需要segment
                        if(StringUtils.isEmpty(cube.getModel().getPartitionDateColumn())){
                            try {
                                buildCubeSegments(new KylinCubeSegment("FULL_BUILD", cube.getCubeName(), null, null), SegmentRequest.BuildType.BUILD);
                            }catch(Exception e){
                                log.error("Full_build error",e);
                            }
                        }else{
                            createCubeSegment(cube.getCubeName(),5,6);
                        }
                    }
                }).start();
                resultMap.put("status", "true");
                resultMap.put("message", stringBuilder.toString());
                ret.add(resultMap);
            } catch (JsonProcessingException | IllegalArgumentException e) {
                log.error(e);
                resultMap.put("status", "false");
                resultMap.put("message", e.getMessage());
                ret.add(resultMap);
                hasError = true;
            }
        }

        if (hasError) {
            throw new RuntimeException(jsonMapper.writeValueAsString(ret));
        }
        return ret;
    }

    public CubeRequest createKylinCube(KylinCube cube) throws Exception {
        CubeDesc2 cubeDesc = transformCube(cube);
        CubeRequest request = new CubeRequest();
        request.setUuid(UUID.randomUUID().toString());
        request.setCubeName(cube.getCubeName());
        request.setCubeDescData(jsonMapper.writeValueAsString(cubeDesc));
        logger.debug(String.format("CubeDescData: %s", request.getCubeDescData()));
        request.setMessage("cube automation");
        request.setSuccessful(true);
        request.setProject(cube.getProject().getProjectName());

        CubeRequest createdCube = kylin.createCube(request);
        return createdCube;
    }

    /**
     * cube 转成kylin使用的对象
     *
     * @param cube
     * @return
     */
    private CubeDesc2 transformCube(KylinCube cube) {
        int count = 0;
        CubeDesc2 cubeDesc = new CubeDesc2();
        cubeDesc.setCubeId(cube.getId());
        cubeDesc.setProjectId(cube.getProjectId());
        cubeDesc.setModelId(cube.getModelId());
        cubeDesc.setName(cube.getCubeName());
        cubeDesc.setModelName(cube.getModel().getModelName());
        cubeDesc.setDescription(cube.getCubeDescription());
        cubeDesc.setNotifyList(Arrays.asList(cube.getNotifyList().split(",")));
        cubeDesc.setStatusNeedNotify(Arrays.asList(cube.getStatusNeedNotify().split(",")));
        cubeDesc.setRetentionRange(cube.getRetentionRange());
        long[] mergeRangeArray = new long[cube.getAutoMergeTimeRanges().split(",").length];
        for (String str : cube.getAutoMergeTimeRanges().split(",")) {
            mergeRangeArray[count] = Long.parseLong(str.trim());
            count++;
        }
        cubeDesc.setAutoMergeTimeRanges(mergeRangeArray);
        cubeDesc.setEngineType(cube.getEngineType());
        cubeDesc.setStorageType(cube.getStorageType());
        // Set dimensions
        List<DimensionDesc> dimensionList = Lists.newArrayList();
        List<KylinDimension> kylinDimensions = cube.getCubeDimensionList();
        Optional.ofNullable(kylinDimensions).ifPresent(kylinDims -> kylinDims.forEach(dim -> {
            DimensionDesc dimensionDesc = new DimensionDesc();
            dimensionDesc.setName(dim.getDimensionName());
            dimensionDesc.setTable(dim.getTable());
            if (StringUtils.startsWithIgnoreCase(dim.getDimensionType(), "normal")) {
                dimensionDesc.setColumn(dim.getColumn());
                dimensionDesc.setDerived(null);
            } else {
                dimensionDesc.setColumn("{FK}");
                dimensionDesc.setDerived(dim.getColumn().split(","));
            }
            dimensionList.add(dimensionDesc);
        }));
        cubeDesc.setDimensions(dimensionList);
        // Set rowkey
        List<RowKeyColDesc> rowKeyColDescList = Lists.newArrayList();
        Optional.ofNullable(cube.getModel().getRowkeys()).ifPresent(rowkeys -> rowkeys.forEach(rowkey -> {
            boolean existed = false;
            RowKeyColDesc rowKeyColDesc = new RowKeyColDesc();
            rowKeyColDesc.setColumn(rowkey.getRowkeyColumn());
            rowKeyColDesc.setEncoding(rowkey.getRowkeyEncoding());
            rowKeyColDesc.setShardBy(rowkey.getRowkeyIssharedby() != null && rowkey.getRowkeyIssharedby() != 0);
            for (RowKeyColDesc desc : rowKeyColDescList) {
                if (desc.equals(rowKeyColDesc)) {
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                rowKeyColDescList.add(rowKeyColDesc);
            }
        }));
        RowKeyDesc rowKeyDesc = new RowKeyDesc();
        rowKeyDesc.setRowkeyColumns(rowKeyColDescList.toArray(new RowKeyColDesc[rowKeyColDescList.size()]));
        cubeDesc.setRowkey(rowKeyDesc);
        // Set measures
        boolean hasCountMeasure = false;
        for (KylinMeasure check : cube.getModel().getMeasures()) {
            if ("COUNT".equalsIgnoreCase(check.getMeasureExpression())) {
                hasCountMeasure = true;
                break;
            }
        }
        if (!hasCountMeasure) {
            KylinMeasure countMeasure = new KylinMeasure();
            countMeasure.setMeasureType("constant");
            countMeasure.setMeasureName("_COUNT_");
            countMeasure.setMeasureColumn("1");
            countMeasure.setMeasureColumnDatatype("bigint");
            countMeasure.setMeasureExpression("COUNT");
            cube.getModel().getMeasures().add(countMeasure);
        }
        List<MeasureDesc> measureList = Lists.newArrayList();
        Optional.ofNullable(cube.getModel().getMeasures()).ifPresent(aa -> aa.forEach(measure -> {
            MeasureDesc measureDesc = new MeasureDesc();
            measureDesc.setName(measure.getMeasureName());
            ParameterDesc parameterDesc = null;
            ParameterDesc2 parameterDesc2;
            String tableAlias = cube.getModel().getFactTable();
            if ("constant".equalsIgnoreCase(measure.getMeasureType())) {
                parameterDesc2 = ParameterDesc2.newInstance(tableAlias, measure.getMeasureColumn());
            } else {
                // TODO: Assemble dynamic array parameter for top-n measure
                parameterDesc2 = ParameterDesc2.newInstance(measure);
            }
            try {
                String param = jsonMapper.writeValueAsString(parameterDesc2);
                parameterDesc = jsonMapper.readValue(param, ParameterDesc.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Can not transform object from " + parameterDesc2);
            }
            FunctionDesc functionDesc = FunctionDesc.newInstance(
                    StringUtils.isBlank(measure.getMeasureExpression()) ? null : measure.getMeasureExpression(),
                    parameterDesc, StringUtils.isBlank(measure.getMeasureColumnDatatype()) ? null : measure.getMeasureColumnDatatype());
            measureDesc.setFunction(functionDesc);
            measureList.add(measureDesc);
        }));
        cubeDesc.setMeasures(measureList);
        cubeDesc.setHbaseMapping(getDefaultHBaseFamily(measureList));
        // Set aggregation groups
        List<AggregationGroup> aggregationGroupList = Lists.newArrayList();
        List<KylinDimensionRowkey> rowkeys = cube.getModel().getRowkeys();
        Optional.ofNullable(cube.getCubeAggGroupList()).ifPresent(cubeAgges -> cubeAgges.forEach(agg -> {
            AggregationGroup aggGroup = new AggregationGroup();
            String[] includeArray = agg.getAggIncludes().split(",");
            List<String> includeList = Lists.newArrayListWithExpectedSize(includeArray.length);
            for (String includeStr : includeArray) {
                includeList.add(getFullColumnName(includeStr, rowkeys));
            }
            includeArray = includeList.toArray(new String[includeList.size()]);
            aggGroup.setIncludes(includeArray);
            SelectRule selectRule = new SelectRule();
            // hierarchy
            if (!StringUtils.isBlank(agg.getHierarchyDims())) {
                List<List<String>> hierarchyDimList = Lists.newArrayList();
                String[] hierarchyDimArray = agg.getHierarchyDims().split(";"); // 多组层次结构以英文分号分割
                for (String hierarchyStr : hierarchyDimArray) {
                    String[] tempStr = hierarchyStr.split(",");
                    List<String> tempList = Lists.newArrayList();
                    for (String temp : tempStr) {
                        tempList.add(getFullColumnName(temp, rowkeys));
                    }
                    hierarchyDimList.add(tempList);
                }
                StringBuffer stringBuffer = new StringBuffer("");
                for (List<String> list : hierarchyDimList) {
                    stringBuffer.append(StringUtils.join(list, ",")).append(";");
                }
                String getHierarchyDims = StringUtils.substringBeforeLast(stringBuffer.toString(), ";");

                String[] hierarchy = getHierarchyDims.replace(" ", "").split(";");
                String[][] hierarchyDims = new String[hierarchy.length][];
                for (int i = 0; i < hierarchy.length; i++) {
                    hierarchyDims[i] = hierarchy[i].split(",");
                }
                selectRule.hierarchyDims = hierarchyDims;
            }
            // mandatory
            if (!StringUtils.isBlank(agg.getMandatoryDims())) {
                String[] mandatoryArray = agg.getMandatoryDims().replace(" ", "").split(",");
                List<String> mandatoryList = new ArrayList<>(mandatoryArray.length);
                for (String mandatoryStr : mandatoryArray) {
                    mandatoryList.add(getFullColumnName(mandatoryStr, rowkeys));
                }
                mandatoryArray = mandatoryList.toArray(new String[mandatoryList.size()]);
                selectRule.mandatoryDims = mandatoryArray;
            }
            // joint
            if (!StringUtils.isBlank(agg.getJointDims())) {
                List<List<String>> jointDimList = Lists.newArrayList();
                String[] jointDimArray = agg.getJointDims().split(";");  // 多组联合维度以英文分号分割
                for (String jointDimStr : jointDimArray) {
                    String[] tempStr = jointDimStr.split(",");
                    List<String> tempList = Lists.newArrayList();
                    for (String temp : tempStr) {
                        tempList.add(getFullColumnName(temp, rowkeys));
                    }
                    jointDimList.add(tempList);
                }
                StringBuffer stringBuffer = new StringBuffer("");
                for (List<String> list : jointDimList) {
                    stringBuffer.append(StringUtils.join(list, ",")).append(";");
                }
                String joinStr = StringUtils.substringBeforeLast(stringBuffer.toString(), ";");
                String[] joint = joinStr.replace(" ", "").split(";");
                String[][] jointDims = new String[joint.length][];
                for (int i = 0; i < joint.length; i++) {
                    jointDims[i] = joint[i].split(",");
                }
                selectRule.jointDims = jointDims;
            }
            aggGroup.setSelectRule(selectRule);
            aggregationGroupList.add(aggGroup);
        }));
        cubeDesc.setAggregationGroups(aggregationGroupList);
        // Set property
        LinkedHashMap<String, String> propMap = Maps.newLinkedHashMap();
        Optional.ofNullable(cube.getCubePropertyList()).ifPresent(cubePropertyList -> cubePropertyList.forEach(prop -> {
            propMap.putIfAbsent(prop.getPropertyName(), prop.getPropertyValue());
        }));
        cubeDesc.setOverrideKylinProps(propMap);

        // Set dictionary
        List<DictionaryDesc2> dictionaryDescList = Lists.newArrayList();
        Optional.ofNullable(cube.getCubeDictionaryList()).ifPresent(dictionaryList -> dictionaryList.forEach(dic -> {
            DictionaryDesc2 dictionaryDesc = new DictionaryDesc2();
            dictionaryDesc.setColumn(dic.getColumn());
            dictionaryDesc.setBuilderClass(dic.getBuilder());
            dictionaryDesc.setReuseColumn(dic.getReuse());
            dictionaryDescList.add(dictionaryDesc);
        }));
        cubeDesc.setDictionaries(dictionaryDescList);

        return cubeDesc;
    }

    private HBaseMappingDesc getDefaultHBaseFamily(List<MeasureDesc> measures) {
        HBaseMappingDesc mapping = new HBaseMappingDesc();
        HBaseColumnFamilyDesc desc = new HBaseColumnFamilyDesc();
        desc.setName("f1");
        HBaseColumnDesc column = new HBaseColumnDesc();
        column.setQualifier("m");
        String[] measureNames = new String[measures.size()];
        for (int i = 0; i < measureNames.length; ++i) {
            measureNames[i] = measures.get(i).getName();
        }
        column.setMeasureRefs(measureNames);
        desc.setColumns(new HBaseColumnDesc[]{column});

        mapping.setColumnFamily(new HBaseColumnFamilyDesc[]{desc});
        return mapping;
    }

    private String getFullColumnName(String columnName, List<KylinDimensionRowkey> rowkeys) {
        String ret = columnName;
        if (!columnName.contains(".")) {
            Optional<KylinDimensionRowkey> findRow = rowkeys.stream().filter(row -> row.getRowkeyColumn().contains(columnName)).findFirst();
            ret = findRow.get().getRowkeyColumn();
        }
        return ret;
    }

    /**
     * 在数据库中新增cube.
     *
     * @param cubeDesc
     * @return
     */
    public CubeDesc2 createCube(CubeDesc2 cubeDesc) {
        KylinCube cube = transformVO2DBCube(cubeDesc);
        System.out.println(cube);
        return cubeDesc;
    }

    /**
     * 转换页面cube模型成数据库对象模型，以便保存到数据库
     *
     * @param cubeDescVO
     * @return
     */
    private KylinCube transformVO2DBCube(CubeDesc2 cubeDescVO) {

        List<KylinCubeAggGroup> aggregationGroupList = Lists.newArrayList();

        List<AggregationGroup> tempAggregationGroup = cubeDescVO.getAggregationGroups();
        for (int i = 0, s = tempAggregationGroup.size(); i < s; i++) {
            AggregationGroup item = tempAggregationGroup.get(i);
            String[][] hierarchyDimArray = item.getSelectRule().hierarchyDims;
            String[][] jointDimsArray = item.getSelectRule().jointDims;
            StringBuilder hierarchyDimStr = new StringBuilder("");
            StringBuilder jointDimsStr = new StringBuilder("");
            List<String> jointDimsList = Lists.newArrayList();
            // 这里要处理多个的情况：
            if (hierarchyDimArray != null) {
                for (String[] hierarch : hierarchyDimArray) {
                    hierarchyDimStr.append(StringUtils.join(hierarch, ",")).append(";");
                }
            }
            if (jointDimsArray != null) {
                for (String[] jointDims : jointDimsArray) {
                    jointDimsStr.append(StringUtils.join(jointDims, ",")).append(";");
                }
            }

            aggregationGroupList.add(new KylinCubeAggGroup(null, cubeDescVO.getName() + "_agg" + (i + 1),
                    String.join(",", item.getIncludes()),
                    StringUtils.substringBeforeLast(hierarchyDimStr.toString(), ";"),
                    StringUtils.join(item.getSelectRule().mandatoryDims, ","),
                    StringUtils.substringBeforeLast(jointDimsStr.toString(), ";"), new Date(), new Date(), cubeDescVO.getCubeId()));

        }

        List<KylinCubeDictionary> cubeDictionaryList = Lists.newArrayList();

        cubeDescVO.getDictionaries().forEach(dictionaryDesc -> {
            cubeDictionaryList.add(new KylinCubeDictionary(null, dictionaryDesc.getColumn(),
                    dictionaryDesc.getBuilderClass(), new Date(), new Date(), cubeDescVO.getCubeId(), dictionaryDesc.getReuseColumn()));
        });

        List<KylinCubeProperty> cubePropertyList = Lists.newArrayList();

        cubeDescVO.getOverrideKylinProps().forEach((mapKey, mapValue) -> {
            cubePropertyList.add(new KylinCubeProperty(null, mapKey, mapValue, new Date(), new Date(), cubeDescVO.getCubeId()));
        });


        KylinCube cubeMeta = new KylinCube();
        KylinModel model = new KylinModel();
        cubeMeta.setId(cubeDescVO.getCubeId());
        cubeMeta.setCubeName(cubeDescVO.getName());
        cubeMeta.setCubeDescription(cubeDescVO.getDescription());
        cubeMeta.setNotifyList(String.join(",", cubeDescVO.getNotifyList()));
        cubeMeta.setStatusNeedNotify(String.join(",", cubeDescVO.getStatusNeedNotify()));
        cubeMeta.setAutoMergeTimeRanges(StringUtils.join(cubeDescVO.getAutoMergeTimeRanges(), ','));
        cubeMeta.setRetentionRange(cubeDescVO.getRetentionRange());
        cubeMeta.setEngineType(cubeDescVO.getEngineType());
        cubeMeta.setStorageType(cubeDescVO.getStorageType());
        if (null == cubeDescVO.getCubeId()) {
            cubeMeta.setCreatedAt(new Date());
        }
        cubeMeta.setUpdatedAt(new Date());
        cubeMeta.setModelId(cubeDescVO.getModelId());
        cubeMeta.setModel(model);
        cubeMeta.setProjectId(cubeDescVO.getProjectId());
        cubeMeta.setCubeAggGroupList(aggregationGroupList);
        cubeMeta.setCubeDictionaryList(cubeDictionaryList);
        cubeMeta.setCubePropertyList(cubePropertyList);

        // 创建model中的rowkeys
        List<KylinDimensionRowkey> dimensionRowkeyList = new ArrayList<>();
        List<RowKeyColDesc> rowkeys = Lists.newArrayList(cubeDescVO.getRowkey().getRowKeyColumns());
        int index = 1;
        int isShard = 0;
        for (RowKeyColDesc rowKeyColDesc : rowkeys) {
            KylinDimensionRowkey kylinDimensionRowkey = new KylinDimensionRowkey();
            kylinDimensionRowkey.setRowkeyColumn(rowKeyColDesc.getColumn());
            kylinDimensionRowkey.setRowkeySort(String.valueOf(index++));
            kylinDimensionRowkey.setModelId(cubeDescVO.getModelId());
            kylinDimensionRowkey.setRowkeyEncoding(rowKeyColDesc.getEncoding());
            if (!rowKeyColDesc.isShardBy() == true) {
                isShard = 0;
            } else {
                isShard = 1;
            }
            kylinDimensionRowkey.setRowkeyIssharedby(isShard);
            dimensionRowkeyList.add(kylinDimensionRowkey);
        }
        ;

        model.setRowkeys(dimensionRowkeyList);

        //添加model measure 详细信息
        List<KylinMeasure> measureList = new ArrayList<>();
        cubeDescVO.getMeasures().forEach(measureDesc -> {
            KylinMeasure measure = new KylinMeasure();
            measure.setMeasureName(measureDesc.getName());
            measure.setMeasureColumn(measureDesc.getFunction().getParameter().getValue());
            measure.setMeasureColumnDatatype(measureDesc.getFunction().getReturnType());
            measure.setMeasureExpression(measureDesc.getFunction().getExpression());
            measure.setMeasureType(measureDesc.getFunction().getParameter().getType());
            measure.setModelId(cubeDescVO.getModelId());
            measureList.add(measure);
        });
        model.setMeasures(measureList);

        return cubeMeta;
    }

    /**
     * 查询cube 在远程kylin上是否存在
     *
     * @param cubeName
     * @param projectName
     * @return
     * @throws Exception
     */
    public boolean queryKylinStatusByCubeName(String cubeName, String projectName) throws Exception {

        CubeInstance cubeInstance = kylin.getCubeByName(projectName, cubeName);
        return cubeInstance != null && StringUtils.isNotEmpty(cubeInstance.getName());
    }

    /**
     * 根据设置的保存时间（年）， 分区间隔（月）来拆分segment
     * @param storageYear  保存时间 默认application.properties 配置 ，（5年）
     * @param rangeMonth    分区间隔 默认 6 （个月）
     * @return  拆分的segments
     */
    public void createCubeSegment(String cubeName,int storageYear,int rangeMonth){
        storageYear = Optional.of(propertyBean.getKylinStorageYear()).orElse(storageYear);
        rangeMonth = Optional.of(propertyBean.getKylinRangeMonth()).orElse(rangeMonth);

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusYears(storageYear);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        ZoneId zoneId = ZoneId.systemDefault();
        while (startDate.compareTo(currentDate) < 0){
            KylinCubeSegment segment = new KylinCubeSegment();
            segment.setStartDate(Date.from(startDate.atStartOfDay(zoneId).toInstant()));
            LocalDate endDate = startDate.plusMonths(6);
            segment.setEndDate(Date.from(endDate.atStartOfDay(zoneId).toInstant()));
            segment.setSegmentName(startDate.format(dateTimeFormatter) + "000000_"+endDate.format(dateTimeFormatter)+"000000");
            segment.setCubeName(cubeName);
            startDate = endDate;

            // 创建
            try {
                buildCubeSegments(segment, SegmentRequest.BuildType.BUILD);
            }catch(Exception e){
                log.error("创建segment[ "+segment.getSegmentName()+" ]出错：",e);
            }
        }
    }

    /**
     * 创建/合并/刷新cube segment
     *
     * @param list   (segment list)
     * @param action ("BUILD", "REFRESH","MERGE"")
     * @return List<Map   <   String   ,   Boolean>> String :SegmentName ,Boolean: success or not
     * @throws Exception
     */
    public List<Map<String, Boolean>> buildCubeSegments(List<KylinCubeSegment> list, SegmentRequest.BuildType action) throws Exception {
        List<Map<String, Boolean>> results = new ArrayList<>();
        for (KylinCubeSegment kylinCubeSegment : list) {
            Map<String, Boolean> map = new HashMap<>();
            try {
                buildCubeSegments(kylinCubeSegment, action);
                map.put(kylinCubeSegment.getSegmentName(), true);
            } catch (Exception e) {
                map.put(kylinCubeSegment.getSegmentName(), false);
            }
            results.add(map);
        }
        return results;
    }

    /**
     * 创建/合并/刷新cube segment
     * {"buildType":"BUILD","startTime":1522555200000,"endTime":1525132800000}
     * @param kylinCubeSegment (segment)
     * @param action           ("BUILD", "REFRESH","MERGE")
     * @return boolean : success or not
     * @throws Exception
     */

    public Boolean buildCubeSegments(KylinCubeSegment kylinCubeSegment, SegmentRequest.BuildType action) throws Exception {

        long startDate = kylinCubeSegment.getStartDate() == null ? 0 : kylinCubeSegment.getStartDate().getTime() + 8 * 60 * 60 * 1000;
        long endDate = kylinCubeSegment.getEndDate() == null ? 0 : kylinCubeSegment.getEndDate().getTime() + 8 * 60 * 60 * 1000;
        SegmentRequest segmentRequest = new SegmentRequest(startDate, endDate, action, kylinCubeSegment.getCubeName());
        kylin.rebuildSegment(segmentRequest);
        return true;
    }

}
