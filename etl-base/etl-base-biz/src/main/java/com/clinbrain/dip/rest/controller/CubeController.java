package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.kylinmetadata.CubeDesc2;
import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinProject;
import com.clinbrain.dip.rest.response.CubeResponse;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.CubeService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cubes")
@Log4j
public class CubeController {

    @Autowired
    private CubeService cubeService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseData list(@RequestParam(name = "limit", defaultValue = "15") Integer limit,
                             @RequestParam(name = "offset", defaultValue = "0") Integer offset,
                             @RequestParam(name = "projectId", required = false) Integer projectId,
                             @RequestParam(name = "search", required = false) String searchText,
                             @RequestParam(name = "orderBy", required = false) String order) {
        try {
            StringBuilder orderBy = new StringBuilder("");
            if (StringUtils.isNotBlank(order)) {
                String field = StringUtils.substringBefore(order, ":").trim();
                String orderTemp = StringUtils.substringAfter(order, ":").trim();
                orderBy.append(field).append(" ");
                if (Boolean.valueOf(order)) {
                    orderBy.append("desc");
                }
            }

            ResponseData.Page cubes = cubeService.selectAllCubes(projectId, searchText, orderBy.toString(), offset, limit);

            return new ResponseData.Builder<ResponseData.Page>(cubes).success();
        } catch (Exception e) {
            return new ResponseData.Builder<Map>(null).error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{cubeId:[\\d]+}", method = RequestMethod.GET)
    public ResponseData selectOne(@PathVariable("cubeId") Integer cubeId) {
        return new ResponseData.Builder<CubeResponse>().data(cubeService.selectOne(cubeId)).success();
    }

    //create or update
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseData edit(@RequestBody CubeDesc2 cubeMeta) {
        try {
            cubeService.createOrUpadte(cubeMeta);
        } catch (Exception e) {
            log.error(e);
            return new ResponseData.Builder<>().data(e).error(e.getMessage());
        }
        return new ResponseData.Builder<>().data(null).success();
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResponseData create(@RequestBody CubeDesc2 cubeDesc) {
        if (cubeDesc != null) {
            cubeService.createCube(cubeDesc);
        }
        return new ResponseData.Builder<CubeDesc2>().data(cubeDesc).success();
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseData drop(@RequestParam("cubeId") Integer id) {
        Boolean delFlag = cubeService.dropCubeById(id);
        return new ResponseData.Builder<Boolean>().data(delFlag).success();
    }

    /**
     * 在kylin上根据mysql中cube表的id创建这个cube
     * @param id
     * @return
     */
    @RequestMapping(value = "/kylin/{id}", method = RequestMethod.POST)
    public ResponseData createKylinCube(@PathVariable("id") int id) {
        List<Map<String, String>> ret = new ArrayList<>();
        KylinCube cube = cubeService.selectCubeById(id);
        List<KylinCube> projectCubes = Lists.newArrayList(cube);
        String projectName = cube.getProject().getProjectName();

        if (projectCubes.size() == 0) {
            return new ResponseData.Builder<String>().data("没有Cube数据.").error("部署错误！");
        }
        try {
            if (!cubeService.checkProjectExist(projectName)) {
                throw new RuntimeException("项目[" + projectName + "]不存在！");
            }
            ret = cubeService.createKylinCubes(projectName, projectCubes);
        } catch (Exception e) {
            return new ResponseData.Builder<String>().data(e.getMessage()).error("部署出错！");
        }

        return new ResponseData.Builder<String>().data("Cube 创建成功！").success();
    }

    @RequestMapping(value = "/kylin", method = RequestMethod.POST)
    public ResponseData createKylinCubeByProject(@RequestBody KylinProject project) {
        List<Map<String, String>> ret = new ArrayList<>();
        List<KylinCube> projectCubes = Lists.newArrayList();

        try {
            if (!cubeService.checkProjectExist(project.getProjectName())) {
                Map<String, String> map = Maps.newLinkedHashMap();
                map.put(project.getProjectName(), "项目不存在,请先部署项目!");
                ret.add(map);
                throw new RuntimeException("项目[" + project.getProjectName() + "]不存在！");
            }

            ResponseData.Page<KylinCube> pageCubes = cubeService.selectAllCubes(project.getId(), null,
                    null, null, null);
            projectCubes = pageCubes.getRows();

            ret = cubeService.createKylinCubes(project.getProjectName(), projectCubes);
        } catch (Exception e) {
            return new ResponseData.Builder<String>().data(e.getMessage()).error("" + projectCubes.size());
        }

        return new ResponseData.Builder<List>().data(ret).success("" + projectCubes.size());
    }

    @RequestMapping(value = "/kylinstatus", method = RequestMethod.GET)
    public ResponseData kylinStatus(@RequestParam String cubeName, @RequestParam String projectName) {
        if (StringUtils.isEmpty(cubeName)) {
            return new ResponseData.Builder<Boolean>().data(false).error("cube名称为空");
        }
        try {
            if (cubeService.queryKylinStatusByCubeName(cubeName, projectName)) {
                return new ResponseData.Builder<Boolean>().data(true).success();
            }
        } catch (Exception e) {
            return new ResponseData.Builder<Boolean>().data(false).error("cube名称为空");
        }

        return new ResponseData.Builder<Boolean>().data(false).error("error");
    }
}