package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.connection.ClientOutput;
import com.clinbrain.dip.connection.RestfulClient;
import com.clinbrain.dip.metadata.RestfulParameter;
import com.clinbrain.dip.rest.bean.PropertyBean;
import com.clinbrain.dip.rest.request.CubeRequest;
import com.clinbrain.dip.rest.request.HiveTableRequest;
import com.clinbrain.dip.rest.request.ModelRequest;
import com.clinbrain.dip.rest.request.ProjectRequest;
import com.clinbrain.dip.rest.request.SegmentRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.apache.kylin.metadata.project.ProjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("kylinAPIService")
public class KylinAPIService {
    private Logger logger = LoggerFactory.getLogger(KylinAPIService.class);

    @Autowired
    private PropertyBean propertyBean;

    private RestfulClient client = null;
    private ObjectMapper jsonMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        client = new RestfulClient(propertyBean.getKylinHostname(), propertyBean.getKylinUsername(), propertyBean.getKylinPassword());
    }

    public ProjectInstance createProject(ProjectRequest request) throws Exception {
        String jsonData = null;
        jsonData = jsonMapper.writeValueAsString(request);
        RestfulParameter parameter = new RestfulParameter("POST", "/projects", jsonData);
        ClientOutput out = client.execute(parameter);
        ProjectInstance projectInstance = jsonMapper.readValue(out.getText(), ProjectInstance.class);
        return projectInstance;
    }

    public ModelRequest createModel(ModelRequest request) throws Exception {
        String jsonData = jsonMapper.writeValueAsString(request);
        RestfulParameter parameter = new RestfulParameter("POST", "/models", jsonData);
        client.execute(parameter);
        return request;
    }

    public CubeRequest createCube(CubeRequest request) throws Exception {
        String jsonData = jsonMapper.writeValueAsString(request);
        RestfulParameter parameter = new RestfulParameter("POST", "/cubes", jsonData);
        client.execute(parameter);
        logger.debug("Create cube " + request + " success...");
        return request;
    }

    public ProjectInstance updateProject(ProjectRequest request) throws Exception {
        String jsonData = jsonMapper.writeValueAsString(request);
        RestfulParameter parameter = new RestfulParameter("PUT", "/projects", jsonData);
        ClientOutput out = client.execute(parameter);
        ProjectInstance projectInstance = jsonMapper.readValue(out.getText(), ProjectInstance.class);
        logger.debug("Update project " + projectInstance + " success...");
        return projectInstance;
    }

    public ProjectInstance getProjectByName(String projectName) throws Exception {
        List<ProjectInstance> projects = getAllProject();
        ProjectInstance ret = new ProjectInstance();
        for (ProjectInstance project : projects) {
            if (projectName.equalsIgnoreCase(project.getName())) {
                ret = project;
                break;
            }
        }
        return ret;
    }

    public List<ProjectInstance> getAllProject() throws Exception {
        return this.getAllProject(0, Integer.MAX_VALUE);
    }


    public List<ProjectInstance> getAllProject(int offset, int limit) throws Exception {
        String url = String.format("/projects?limit=%d&offset=%d", limit, offset);
        RestfulParameter parameter = new RestfulParameter("GET", url, null);
        List<ProjectInstance> projects;
        ClientOutput out = client.execute(parameter);
        projects = jsonMapper.readValue(out.getText(), new TypeReference<List<ProjectInstance>>() {
        });
        return projects;
    }

    public CubeInstance getCubeByName(String projectName, String cubeName) throws Exception {
        String url = String.format("/cubes?limit=%d&offset=%d&projectName=%s&cubeName=%s", 1, 0, projectName, cubeName);
        RestfulParameter parameter = new RestfulParameter("GET", url, null);
        List<CubeInstance> cubes = null;
        ClientOutput out = client.execute(parameter);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        cubes = jsonMapper.readValue(out.getText(), new TypeReference<List<CubeInstance>>() {
        });
        if (cubes == null || cubes.size() == 0) {
            return null;
        }
        return cubes.get(0);
    }

    public List<CubeInstance> getProjectCubes(String projectName) throws Exception {
        return this.getProjectCubes(projectName, 0, Integer.MAX_VALUE);
    }

    public List<CubeInstance> getProjectCubes(String projectName, int offset, int limit) throws Exception {
        String url = String.format("/cubes?limit=%d&offset=%d&projectName=%s", limit, offset, projectName);
        RestfulParameter parameter = new RestfulParameter("GET", url, null);
        List<CubeInstance> cubes = null;
        ClientOutput out = client.execute(parameter);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        cubes = jsonMapper.readValue(out.getText(), new TypeReference<List<CubeInstance>>() {
        });
        return cubes;
    }

    public DataModelDesc getModeByName(String projectName, String modelName) throws Exception {
        String url = String.format("/models?limit=%d&offset=%d&projectName=%s&modelName=%s", 1, 0, projectName, modelName);
        List<DataModelDesc> models = null;
        RestfulParameter parameter = new RestfulParameter("GET", url, null);
        ClientOutput out = client.execute(parameter);
        models = jsonMapper.readValue(out.getText(), new TypeReference<List<DataModelDesc>>() {
        });
        if (models == null || models.isEmpty()) {
            return null;
        }
        return models.get(0);
    }

    public String deleteModel(String modelName) throws Exception {
        String url = String.format("/models/%s", modelName);
        RestfulParameter parameter = new RestfulParameter("DELETE", url, null);
        ClientOutput out = client.execute(parameter);
        logger.debug("delete bean " + modelName + " success...");
        return out.getText();
    }

    public String deleteCube(String cubeName) throws Exception {
        String url = String.format("/cubes/%s", cubeName);
        RestfulParameter parameter = new RestfulParameter("DELETE", url, null);
        ClientOutput out = client.execute(parameter);
        logger.debug("delete cube " + cubeName + " success...");
        return out.getText();
    }

    public String loadHiveTables(String hiveTables, String projectName) throws Exception {
        String url = String.format("/tables/%s/%s", hiveTables, projectName);
        String param = jsonMapper.writeValueAsString(new HiveTableRequest(true));
        logger.debug("Post parameter : " + param);
        RestfulParameter parameter = new RestfulParameter("POST", url, param);
        ClientOutput out = client.execute(parameter);
        return out.getText();
    }

    public SegmentRequest rebuildSegment(SegmentRequest request) throws Exception {
        String url = String.format("/cubes/%s/build", request.getCubeName());
        String jsonData = jsonMapper.writeValueAsString(request);
        RestfulParameter parameter = new RestfulParameter("PUT",url,jsonData);
        client.execute(parameter);
        return request;
    }
}
