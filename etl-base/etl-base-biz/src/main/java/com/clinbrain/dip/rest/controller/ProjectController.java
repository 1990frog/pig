package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.KylinProject;
import com.clinbrain.dip.pojo.KylinProjectSchema;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.metadata.project.ProjectInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseData list(@RequestParam(name = "limit",defaultValue = "15",required = false)Integer limit,
                             @RequestParam(name = "offset",defaultValue = "0",required = false) Integer offset){
        try {
            Page<KylinProject> projects = projectService.selectProjects(limit,offset);
            return new ResponseData.Builder<ResponseData.Page>(new ResponseData.Page<KylinProject>(projects.getTotal(),projects)).success();
        }  catch (Exception e){
            return new ResponseData.Builder<Map>(null).error(e.getMessage());
        }
    }

    @RequestMapping(value = "/pageData",method = RequestMethod.GET)
    public ResponseData.Page list4BsTable(@RequestParam(name = "limit",defaultValue = "15",required = false)Integer limit,
                                          @RequestParam(name = "offset",defaultValue = "0",required = false) Integer offset){
        try {
            Page<KylinProject> projects = projectService.selectProjects(limit,offset);
            return new ResponseData.Page<KylinProject>(projects.getTotal(),projects);
        }  catch (Exception e){
            return null;
        }
    }

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseData listReadable(){
        List<KylinProject> projects = projectService.selectAllProject();
        return new ResponseData.Builder<List>(projects).success();
    }

    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseData edit(@RequestBody KylinProject project){
        KylinProject kylinProject = projectService.editDBProject(project);
        if(project.getId()==null){
            for (int i = 0; i <project.getSchemas().size() ; i++) {
                KylinProjectSchema schema=project.getSchemas().get(i);
                schema.setSchemaName(schema.getSchemaName());
                schema.setProjectId(projectService.getProIdByProName(project.getProjectName()));
                projectService.newCreateKylinProjectSchema(schema);
            }
        }
        return new ResponseData.Builder<String>().data("").success();
    }

    @RequestMapping(value = "",method = RequestMethod.DELETE)
    public ResponseData drop(@RequestParam("projectId") Integer id){
        return new ResponseData.Builder<Boolean>().data(projectService.deleteDBProject(id) > 0).success();
    }

    //create KylinProject
    @RequestMapping(value = "/kylin/{projectId}",method = RequestMethod.POST)
    public ResponseData createKylinProject(@PathVariable("projectId") Integer projectId) throws JsonProcessingException {
        try{
            projectService.createKylinProject(projectId);
        }catch (Exception e){
            return new ResponseData.Builder<String>().error(e.getMessage());
        }
        return new ResponseData.Builder<String>().success("部署成功！");
    }

    @RequestMapping(value = "",method = RequestMethod.PUT)
    public ResponseData updateKylinProject(@RequestBody KylinProject project) throws Exception {
        return new ResponseData.Builder<ProjectInstance>()
                .data(projectService.updateKylinProject(project)).success();
    }


    @RequestMapping(value = "/getKylinSchema",method = RequestMethod.GET)
    public ResponseData getKylinProjectSchemaName(@RequestParam("projectId") Integer id){
        List<String> kylinProjectSchemaNames = projectService.getKylinProjectSchemaName(id);
        return new ResponseData.Builder<List>(kylinProjectSchemaNames).success();
    }


    //调用kylin的api查询是否kylin上是否存在项目名为projectName的方法
    @RequestMapping(value = "/kylinProStatus",method = RequestMethod.GET)
    public ResponseData kylinProStatus(@RequestParam String projectName){
        if(StringUtils.isEmpty(projectName)){
            return new ResponseData.Builder<Boolean>().data(false).error("project名称为空");
        }
        try {
            if(projectService.queryKylinStatusByProjectName(projectName)){
                return new ResponseData.Builder<Boolean>().data(true).success();
            }
        }catch(Exception e){
            return new ResponseData.Builder<Boolean>().data(false).error("project名称为空");
        }
        return new ResponseData.Builder<Boolean>().data(false).error("error");
    }


}
