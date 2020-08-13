package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.KylinDimension;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.DimensionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dimension")
public class DimensionController {

    @Autowired
    private DimensionService dimensionService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseData list(@RequestParam(name = "limit", defaultValue = "15") Integer limit,
                             @RequestParam(name = "offset", defaultValue = "0") Integer offset,
                             @RequestParam(name = "search", required = false) String searchText,
                             @RequestParam(name = "orderBy", required = false) String orderBy) {
        try {
            Example example = new Example(KylinDimension.class);
            example.orderBy("schema").orderBy("table").orderBy("dimensionType");
            if (StringUtils.isNotBlank(searchText)) {
                searchText = "%" + searchText + "%";
                example.and(example.createCriteria().orLike("table", searchText)
                        .orLike("dimensionName", searchText)
                        .orLike("dimensionNameAlias", searchText));
            }

            if (StringUtils.isNotBlank(orderBy)) {
                String field = StringUtils.substringBefore(orderBy, ":").trim();
                String order = StringUtils.substringAfter(orderBy, ":").trim();
                if (Boolean.valueOf(order)) {
                    example.orderBy(field).desc();
                } else {
                    example.orderBy(field).asc();
                }
            }

            List<KylinDimension> dimensionList = dimensionService.selectAllDimension(example, limit, offset);

            return new ResponseData.Builder<List>(dimensionList).success();
        } catch (Exception e) {
            return new ResponseData.Builder<Map>(null).error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id:[\\d]+}", method = RequestMethod.GET)
    public ResponseData one(@PathVariable("id") Integer id) {

        return new ResponseData.Builder<KylinDimension>().data(dimensionService.selectDimensionById(id)).success();
    }

    /**
     * 新增或者修改dimension
     *
     * @param dimension
     * @return
     */
    @CacheEvict(cacheNames = "cubes", allEntries = true)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseData edit(@RequestBody KylinDimension dimension) {
        if (dimension.getId() == null) {
            List<KylinDimension> dimensionDerivedColumns = dimension.getDimensionColumns();
            dimensionDerivedColumns.forEach(derivedColumn -> {
                if (derivedColumn.getId() != null) {
                    dimensionService.deleteDimensionByTable(derivedColumn.getTable());
                }
            });
        }
        return new ResponseData.Builder<KylinDimension>().data(dimensionService.editDimension(dimension)).success();
    }

    @CacheEvict(cacheNames = "cubes", allEntries = true)
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseData drop(@RequestParam("dimId") String[] id) throws Exception {
        String name = "";
        try {
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < id.length; i++) {
                list.add(Integer.parseInt(id[i]));
            }
            List<KylinModel> models = dimensionService.selectModelDimensionByDimensionId(list);
            for (int i = 0; i < models.size(); i++) {
                KylinModel kylinModel = models.get(i);
                if (kylinModel.getModelName() != null) {
                    name += kylinModel.getModelName() + "、";
                }
            }
            if (models.size() > 0) {
                return new ResponseData.Builder<>().data(name).error("不能删除");
            } else {
                for (int i = 0; i < id.length; i++) {
                    Integer number = Integer.parseInt(id[i]);
                    dimensionService.deleteDimension(number);
                }
                return new ResponseData.Builder<>().data(true).success("删除成功");
            }
        } catch (Exception e) {
            throw new Exception("错误");
        }
    }

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    public ResponseData selectDimsByModelId(@RequestParam("modelId") Integer modelId) {
        return new ResponseData.Builder<List>().data(dimensionService.selectDimsByModelId(modelId)).success();

    }

    @RequestMapping(value = "/dim", method = RequestMethod.POST)
    public ResponseData selectDimensionsMap() {
        return new ResponseData.Builder<List<KylinDimension>>().data(dimensionService.selectDimensionsMap()).success();
    }
}
