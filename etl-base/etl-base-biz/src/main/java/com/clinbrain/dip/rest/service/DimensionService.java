package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinDimensionRowkey;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.rest.mapper.DBDimensionMapper;
import com.clinbrain.dip.rest.mapper.DBDimensionRowkeyMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class DimensionService extends BaseService<KylinDimension> {

    @Autowired
    @Qualifier("DimensionMapper")
    private DBDimensionMapper dimensionMapper;

    @Autowired
    @Qualifier("DimensionRowkeyMapper")
    private DBDimensionRowkeyMapper dimensionRowkeyMapper;


    /**
     * 由于需要转换数据，不使用分页
     *
     * @param example
     * @param limit
     * @param offset
     * @return
     */
    @CacheEvict(cacheNames = "hiveTables", allEntries = true)
    @Cacheable(cacheNames = "dimensions")
    public List<KylinDimension> selectAllDimension(Example example, int limit, int offset) {
        if (example == null) {
            example = new Example(KylinDimension.class);
            example.createCriteria().andEqualTo("dimensionType", "derived")
                    .orLike("dimensionType", "normal%");
        } else {
            example.and(example.createCriteria().andEqualTo("dimensionType", "derived")
                    .orLike("dimensionType", "normal%"));
        }
        example.orderBy("schema").orderBy("table").orderBy("dimensionType");

        List<KylinDimension> dimensionsList = getDimensions(example);
        List<KylinDimension> ret = Lists.newArrayList();
        Map<String, String> tables = Maps.newLinkedHashMap();
        dimensionsList.forEach(dim -> tables.put(String.format("%s.%s", dim.getSchema(), dim.getTable()), dim.getTable()));
        for (Map.Entry<String, String> table : tables.entrySet()) {
            List<KylinDimension> derivedDimensionList = Lists.newArrayList();
            KylinDimension derivedDimension = new KylinDimension();
            long timestamp = 0L;
            for (KylinDimension dimension : dimensionsList) {
                if (table.getKey().equalsIgnoreCase(String.format("%s.%s", dimension.getSchema(), dimension.getTable()))) {
                    if (StringUtils.startsWithIgnoreCase(dimension.getDimensionType(), "normal"))
                        ret.add(dimension);
                    else {
                        derivedDimensionList.add(dimension);
                        if (StringUtils.isBlank(derivedDimension.getSchema()))
                            derivedDimension.setSchema(dimension.getSchema());
                        if (StringUtils.isBlank(derivedDimension.getDimensionName()))
                            derivedDimension.setDimensionName(dimension.getDimensionName());
                        if (StringUtils.isBlank(derivedDimension.getTable())) {
                            derivedDimension.setTable(dimension.getTable());
                        }
                        timestamp = Math.max(timestamp, dimension.getUpdatedAt().getTime());
                    }
                }
            }
            if (derivedDimensionList.size() > 0) {
                derivedDimension.setDimensionType("derived");
                derivedDimension.setUpdatedAt(timestamp == 0L ? new Date() : new Timestamp(timestamp));
                derivedDimension.setDimensionColumns(derivedDimensionList);
                ret.add(derivedDimension);
            }
        }
        return ret;
        /*
        Map<String,Object> resultDimensionsMap = new HashMap<>();
        List<KylinDimension> dimensionsList = getDimensions(example);

        KylinDimension lastDerivedDimensionItem = null;
        List<KylinDimension> dimensionColumns = new ArrayList<>();
        List<KylinDimension> removeDimensions = new ArrayList<>();
        for(int i = 0; i < dimensionsList.size(); i ++){
            KylinDimension dimensionItem = dimensionsList.get(i);

            if("derived".equalsIgnoreCase(dimensionItem.getDimensionType())){
                if(lastDerivedDimensionItem == null
                        || (StringUtils.isNotBlank(dimensionItem.getSchema())
                        && !dimensionItem.getSchema().equalsIgnoreCase(lastDerivedDimensionItem.getSchema()))
                        || !dimensionItem.getTable().equalsIgnoreCase(lastDerivedDimensionItem.getTable())){
                    lastDerivedDimensionItem = dimensionItem;
                    dimensionColumns = Lists.newArrayList();
                    KylinDimension copyObject = new KylinDimension();
                    BeanUtils.copyProperties(dimensionItem,copyObject);
                    dimensionColumns.add(copyObject);
                    lastDerivedDimensionItem.setDimensionColumns(dimensionColumns);
                }else{
                    KylinDimension copyObject = new KylinDimension();
                    BeanUtils.copyProperties(dimensionItem,copyObject);
                    dimensionColumns.add(copyObject);
                    removeDimensions.add(dimensionItem);
                }
            }else if(lastDerivedDimensionItem != null){
                lastDerivedDimensionItem = null;
                dimensionColumns = null;
            }
        }

        dimensionsList.removeAll(removeDimensions);

        return dimensionsList;
        */
    }

    /**
     * 查询所有dimensions，增加缓存，提供后续分析，根据类型列重新整理显示条数
     * 同一数据库。表 的derived类型数据行放到同一行
     *
     * @return
     */
    @Cacheable("dimensions")
    public List<KylinDimension> getDimensions(Example example) {

        return dimensionMapper.selectByExample(example);
    }

    @Cacheable(cacheNames = "dimensions", key = "'dimension_'+#id")
    public KylinDimension selectDimensionById(Integer id) {
        KylinDimension dimension = new KylinDimension();
        dimension.setId(id);
        return dimensionMapper.selectByPrimaryKey(id);
    }

    @CacheEvict(cacheNames = "dimensions", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public KylinDimension editDimension(KylinDimension dimension) {
        // 新增
        if (null == dimension.getId()) {
            //如果是normal,直接单个保存
            if (StringUtils.startsWithIgnoreCase(dimension.getDimensionType(), "normal")) {
                dimensionMapper.insert(dimension);
            } else {

                List<KylinDimension> dimensionDerivedColumns = dimension.getDimensionColumns();
                dimensionDerivedColumns.forEach(derivedColumn -> {
                    dimension.setColumn(derivedColumn.getColumn());
                    dimension.setColumnDatatype(derivedColumn.getColumnDatatype());
                    dimension.setDimensionNameAlias(derivedColumn.getDimensionNameAlias());
                    dimensionMapper.insert(dimension);
                });
            }
        } else {
            if (StringUtils.startsWithIgnoreCase(dimension.getDimensionType(), "normal")) {
                dimensionMapper.updateByPrimaryKeySelective(dimension);
            } else {
                // 1.操作新增，修改。删除的列
                List<Integer> removeIds = new ArrayList<>();
                dimension.getDimensionColumns().forEach(derivedColumn -> {
                    if (null == derivedColumn.getId()) {  //新增的数据
                        derivedColumn.setSchema(dimension.getSchema());
                        derivedColumn.setTable(dimension.getTable());
                        derivedColumn.setDimensionName(dimension.getDimensionName());
                        derivedColumn.setDimensionType(dimension.getDimensionType());
                        dimensionMapper.insert(derivedColumn);
                    } else {
                        if (derivedColumn.getIsDeleted()) {
                            dimensionMapper.deleteByPrimaryKey(derivedColumn);
                        } else {
                            derivedColumn.setDimensionName(dimension.getDimensionName());

                            derivedColumn.setTable(dimension.getTable());

                            dimensionMapper.updateByPrimaryKeySelective(derivedColumn);
                        }
                    }
                });
            }
        }
        return dimension;
    }

    @CacheEvict(cacheNames = "dimensions", allEntries = true)
    public boolean deleteDimension(Integer id) {
        KylinDimension dimension = new KylinDimension();
        dimension.setId(id);
        return dimensionMapper.deleteByPrimaryKey(dimension) > 0;
    }

    public List<KylinDimension> selectDimsByModelId(Integer modelId) {
        return dimensionMapper.selectDimsByModelId(modelId);
    }

    public boolean insertModelDimension(Integer modelDimensionId, Integer modelId) throws Exception{
        if (dimensionMapper.insertModelDimension(modelDimensionId, modelId)) {
            List<KylinDimensionRowkey> list = this.selectDimensionAsRowkey(modelId);
            dimensionMapper.deleteDimensionRowkeyByModelId(modelId);
            for (int i = 0; i < list.size(); i++) {
                KylinDimensionRowkey rowkey = list.get(i);
                rowkey.setRowkeyColumn(rowkey.getRowkeyColumn());
                rowkey.setRowkeySort("1000");
                rowkey.setRowkeyIssharedby(0);
                rowkey.setRowkeyEncoding("dict");
                rowkey.setModelId(rowkey.getModelId());
                dimensionMapper.appendDimensionRowkey(rowkey);
            }
            return true;
        }
        return false;
    }

    public List<KylinDimensionRowkey> selectDimensionAsRowkey(Integer modelId) {
        return dimensionMapper.selectDimensionAsRowkey(modelId);
    }

    /**
     * ---- dimension rowkey 相关
     */

    public List<KylinDimensionRowkey> getDimRowkeysByModelId(Integer modelId) {
        Example example = new Example(KylinDimensionRowkey.class);
        if (null != modelId) {
            example.createCriteria().andEqualTo("modelId", modelId);
        }
        return dimensionRowkeyMapper.selectByExample(example);
    }

    public List<KylinDimension> selectDimensionsMap() {
        return dimensionMapper.selectAll();
    }

    public void deleteDimensionByTable(String table){
         dimensionMapper.deleteDimensionByTable(table);
    }

    public List<KylinModel> selectModelDimensionByDimensionId(List<Integer> id){
        return dimensionMapper.selectModelDimensionByDimensionId(id);
    }
}
