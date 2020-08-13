package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinModelLookup;
import com.clinbrain.dip.rest.request.ModelRequest;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.DimensionService;
import com.clinbrain.dip.rest.service.KylinAPIService;
import com.clinbrain.dip.rest.service.ModelService;
import org.apache.commons.lang.StringUtils;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liaopan on 2017/10/19.
 */
@RequestMapping("models")
@RestController
public class ModelController {
    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    private ModelService modelService;

    @Autowired
    private KylinAPIService kylin;

    @Autowired
    private DimensionService dimensionService;

    @RequestMapping("/{id}")
    public ResponseData selectModelById(@PathVariable("id") Integer id) {
        return new ResponseData.Builder<KylinModel>().data(modelService.selectModelById(id)).success();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseData selectOnlyModelsByProjectId(@RequestParam("projectId") Integer projectId) {
        return new ResponseData.Builder<List>().data(modelService.selectOnlyModelByProjectId(projectId)).success();
    }

    /*
     * Delete Model By ID
     * */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseData deleteModelById(@PathVariable("id") Integer id) throws Exception {
        KylinCube cube= modelService.selectKylinCubeByModelId(id);
        try {
            if(cube!=null){
                return new ResponseData.Builder<>(cube.getCubeName()).error("error");
             }
            modelService.deleteModel(id);
            return new ResponseData.Builder<>().success("成功");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new Exception("删除失败. " + " 失败原因: " + e.getMessage(), e);
        }
    }

    /*Query All Model info*/
    @RequestMapping("/get")
    public ResponseData queryAllModel(Integer projectId) {
        return new ResponseData.Builder<List<KylinModel>>().data(modelService.selectModelByProjectId(projectId)).success();
    }

    /*
     *Add Model Info
     * */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public ResponseData createModels(@RequestBody KylinModel kylinModel) throws Exception {
        List<KylinModelLookup> lookupTables = new ArrayList<KylinModelLookup>();
        String factName = kylinModel.getFactTable().split("\\.")[1];
        kylinModel.setFactTable(factName);
        try{
            if (modelService.newCreateModel(kylinModel)) {
                return new ResponseData.Builder<>().data("succ").success();
            }
            return new ResponseData.Builder<>().error("添加失败");
        }catch (Exception e){
            return new ResponseData.Builder<>().error("添加失败");
        }
    }

    /*
     * Update Model INFO
     * 更新时清除cube缓存
     *
     * */
    @CacheEvict(cacheNames = "cubes", allEntries = true)
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @Transactional(rollbackFor = Exception.class)
    public ResponseData updateModel(@RequestBody KylinModel kylinModel) {
        String factName = kylinModel.getFactTable().split("\\.")[1];
        kylinModel.setFactTable(factName);
        try{
            if (modelService.updateModelInfo(kylinModel)) {
                return new ResponseData.Builder<>().error("更新成功");
            } else {
                return new ResponseData.Builder<>().success("更新失败");
            }
        }catch (Exception e){
            return new ResponseData.Builder<>().error("更新失败");
        }
    }

    /**
     * Create a bean in Kylin
     *
     * @param id
     */
    @RequestMapping(value = "/kylin/{id}", method = RequestMethod.POST)
    public ResponseData createKylinModel(@PathVariable("id") Integer id) throws Exception {
        List<KylinModel> models = modelService.getModels();
        KylinModel model = new KylinModel();
        for (KylinModel m : models) {
            if (m.getId() == id) {
                model = m;
                break;
            }
        }
        if (model == null)
            logger.info("bean is not allow null Object...");

        DataModelDesc dataModelDesc = kylin.getModeByName(model.getProjectName(), model.getModelName());
        if (dataModelDesc != null)
            logger.error("bean is existed ...");

        ModelRequest request = modelService.createKylinModel(model);
        return new ResponseData.Builder<ModelRequest>().data(request).success();
    }

    /**
     * 克隆model，
     *
     * @param modelId   需要克隆的model id
     * @param projectId clone到目标项目
     * @return 克隆后的model
     */
    @RequestMapping(value = "/clone", method = RequestMethod.GET)
    public ResponseData clone(@RequestParam Integer modelId, @RequestParam Integer projectId, @RequestParam String newModelName) {
        // 1. 查找model ，2. 通过sql insert ... select 插入数据 （多个表事务）
        KylinModel modelMeta = modelService.selectModelByNames(newModelName);
        if (modelMeta == null || StringUtils.isEmpty(newModelName)) {
            try {
                modelService.clone(modelId, projectId, newModelName);
                return new ResponseData.Builder().success("克隆成功！");
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseData.Builder().error("克隆出错！");
            }
        }
        return new ResponseData.Builder().error("不能添加相同的模型名称！");
    }
}
