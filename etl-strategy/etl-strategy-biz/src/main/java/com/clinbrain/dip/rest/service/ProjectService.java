package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.connection.BeelineHiveClient;
import com.clinbrain.dip.pojo.KylinProject;
import com.clinbrain.dip.pojo.KylinProjectSchema;
import com.clinbrain.dip.rest.mapper.DBProjectMapper;
import com.clinbrain.dip.rest.request.ProjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.metadata.project.ProjectInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j
public class ProjectService {
    @Autowired
    private DBProjectMapper dbProjectMapper;

    @Autowired
    private KylinAPIService kylin;

    @Autowired
    private DBProjectMapper projectMapper;

    @Autowired
    private BeelineHiveClient hiveClient;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public List<KylinProject> selectAllProject() {
        return dbProjectMapper.selectAllProject();
    }

    public Page<KylinProject> selectProjects(Integer limit, Integer offset) {
        PageHelper.offsetPage(offset, limit);
        return (Page<KylinProject>) dbProjectMapper.selectAllProject();
    }

    public List<KylinProject> selectProjectByName(String projectName) {
        Example example = new Example(KylinProject.class);
        example.createCriteria().andEqualTo("projectName", projectName);
        return dbProjectMapper.selectByExample(example);
    }

    public KylinProject editDBProject(KylinProject project) {
        if (null != project.getId()) {
            List<String> hus = dbProjectMapper.getKylinProjectSchemaName(project.getId());
            dbProjectMapper.deleteKylinProjectSchema(project.getId());
            for (int i = 0; i < project.getSchemas().size(); i++) {
                KylinProjectSchema schema = project.getSchemas().get(i);
                schema.setSchemaName(schema.getSchemaName());
                schema.setProjectId(project.getId());
                dbProjectMapper.newCreateKylinProjectSchema(schema);
            }
            dbProjectMapper.updateByPrimaryKeySelective(project);

        } else {
            dbProjectMapper.insertSelective(project);
        }
        return project;
    }

    public int deleteDBProject(Integer id) {
        if (dbProjectMapper.selectKylinProjectSchema(id) > 0) {
            dbProjectMapper.deleteKylinProjectSchema(id);
        }
        KylinProject project = new KylinProject();
        project.setId(id);
        if(!(dbProjectMapper.selectCubeCountsByProId(id)>0 || dbProjectMapper.selectCubeCountsByProId(id)>0)){
            return dbProjectMapper.deleteByPrimaryKey(project);
        }
        return 0;
    }

    public ProjectInstance createKylinProject(Integer projectId) throws Exception {
        KylinProject kylinProject = dbProjectMapper.selectByPrimaryKey(projectId);
        List<KylinProjectSchema> kylinProjectSchemas = dbProjectMapper.getKylinProjectSchema(projectId);
        kylinProject.setSchemas(kylinProjectSchemas);
        ProjectInstance projectInstance = new ProjectInstance();
        projectInstance.setName(kylinProject.getProjectName());
        projectInstance.setDescription(kylinProject.getProjectDescription());
        ProjectRequest request = new ProjectRequest();
        request.setProjectDescData(jsonMapper.writeValueAsString(projectInstance));
        ProjectInstance projectTemp = kylin.getProjectByName(kylinProject.getProjectName());
        if (projectTemp == null || StringUtils.isEmpty(projectTemp.getName())) {
            projectTemp = kylin.createProject(request);
        }
        if (projectTemp != null) {
            // 加载hive数据表  POST /kylin/api/tables/{tables}/{project}
            List<String> hiveTables = new ArrayList<>();
            //赋值
            kylinProject.getSchemas().forEach(s -> {
                try {
                    hiveTables.addAll(hiveClient.getTableNames(s.getSchemaName())
                            .stream().map(table -> s.getSchemaName() + "." + table).collect(Collectors.toList()));
                } catch (Exception e) {
                    log.error(e);
                }
            });
            kylin.loadHiveTables(StringUtils.join(hiveTables, ","), kylinProject.getProjectName());
        }
        return projectTemp;
    }

    public ProjectInstance updateKylinProject(KylinProject project) throws Exception {
        ProjectInstance projectInstance = new ProjectInstance();
        projectInstance.setName(project.getProjectName());
        projectInstance.setDescription(project.getProjectDescription());
        ProjectRequest request = new ProjectRequest();
        request.setProjectDescData(jsonMapper.writeValueAsString(projectInstance));
        ProjectInstance updatedProj = kylin.updateProject(request);
        return updatedProj;
    }

    public KylinProject GetDataBaseByProName(Integer proId) {
        return projectMapper.GetDataBaseByProName(proId);
    }

    public int getProIdByProName(String projectName) {
        return dbProjectMapper.getProIdByProName(projectName);
    }

    public boolean newCreateKylinProjectSchema(KylinProjectSchema kylinProjectSchema) {
        return dbProjectMapper.newCreateKylinProjectSchema(kylinProjectSchema);
    }

    public List<String> getKylinProjectSchemaName(Integer projectId) {
        List<String> list = dbProjectMapper.getKylinProjectSchemaName(projectId);
        return list;
    }

    /**
     * check projectName is or not
     */
    public boolean queryKylinStatusByProjectName(String projectName) throws Exception  {
        ProjectInstance projectInstance  = kylin.getProjectByName(projectName);
        return projectInstance != null && StringUtils.isNotEmpty(projectInstance.getName());
    }
}
