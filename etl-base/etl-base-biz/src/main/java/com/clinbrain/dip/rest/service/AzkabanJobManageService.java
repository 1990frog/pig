package com.clinbrain.dip.rest.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.clinbrain.dip.metadata.azkaban.Flow;
import com.clinbrain.dip.metadata.azkaban.Project;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.util.SSLUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class AzkabanJobManageService {
	@Value("${azkabanJobManage.serverAddress}")
	private String serverAddress;
	@Value("${azkabanJobManage.sessionId}")
	private String sessionId;

	private String zipFilePath = System.getProperty("java.io.tmpdir");
	@Autowired
	@Qualifier("azkabanRestTemplate")
	private RestTemplate restTemplate;

	/**
	 * 登录
	 * 返回sessionID
	 * @throws Exception
	 */
	public ResponseData<String> login(String username,String password) throws Exception {
		SSLUtil.turnOffSslChecking();
		HttpHeaders hs = new HttpHeaders();
		hs.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		hs.add("X-Requested-With", "XMLHttpRequest");
		LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<String, String>();
		linkedMultiValueMap.add("action", "login");
		linkedMultiValueMap.add("username", username);
		linkedMultiValueMap.add("password", password);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(linkedMultiValueMap, hs);
		String postForObject = restTemplate.postForObject(serverAddress, httpEntity, String.class);
		return new ResponseData.Builder<String>(postForObject).success();
	}

	/**
	 * 创建一个project
	 * @return
	 *
	 * @throws Exception
	 */

	public ResponseData<String> createProject(Project project) throws Exception {
		if(isExistProject(project)){
			return new ResponseData.Builder("project："+project.getName()+"已经存在！").error("project："+project.getName()+"已经存在！");
		}
		SSLUtil.turnOffSslChecking();
		HttpHeaders hs = new HttpHeaders();
		hs.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		hs.add("X-Requested-With", "XMLHttpRequest");
		LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<String, String>();
		linkedMultiValueMap.add("action","create");
		linkedMultiValueMap.add("session.id", sessionId);
		linkedMultiValueMap.add("name", project.getName());
		linkedMultiValueMap.add("description", project.getDescription());
		linkedMultiValueMap.add("groups", project.getGroups());
		HttpEntity<LinkedMultiValueMap<String, String>> httpEntity = new HttpEntity<LinkedMultiValueMap<String, String>>(linkedMultiValueMap, hs);
		String postForObject = restTemplate.postForObject(serverAddress + "/manager", httpEntity, String.class);
		return new ResponseData.Builder(postForObject).success();
	}
	/**
	 * 创建或更新一个project
	 *
	 * @return
	 *
	 * @throws Exception
	 */

	public ResponseData<String> createOrUpdateProject(Project project) throws Exception {
		//判断是否存在
		Integer createResult=null;
		if(!isExistProject(project)){
			//创建project
			createResult=createProject(project).getStatus();
			if(ResponseData.Status.SUCCESS.getCode()!=createResult){
				return new ResponseData.Builder("project："+project.getName()+"创建不成功！").error("project："+project.getName()+"创建不成功！");
			}
		}
		//上传zip
		if(project.getZipFileName()!=null){
			createResult = uploadZip(project.getZipFileName(), project.getName()).getStatus();
			if(ResponseData.Status.SUCCESS.getCode()!=createResult){
				return new ResponseData.Builder("project："+project.getName()+"的zip包："+project.getZipFileName()+"上传不成功！").error("project："+project.getName()+"的zip包："+project.getZipFileName()+"上传不成功！");
			}
		}

		//查询project的flows
		project.setFlows(getProjectFlows(project));
		//如果没有flows 就创建project 不调度
		if(project.getFlows()==null||project.getFlows().size()==0){
			return new ResponseData.Builder("project创建成功，没有flow调度").success("project创建成功，没有flow调度");
		}
		//如果没有flows 就创建project 不调度
		if(project.getCronExpression()==null){
			return new ResponseData.Builder("project创建成功，cron表达式为空！").success("project创建成功，cron表达式为空！");
		}
		SSLUtil.turnOffSslChecking();
		int success=0;
		int error=0;
		String message="";
		LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<String, Object>();
		linkedMultiValueMap.add("session.id", sessionId);
		linkedMultiValueMap.add("ajax", "scheduleCronFlow");
		linkedMultiValueMap.add("projectName", project.getName());
		for(Flow flow:project.getFlows()){
			linkedMultiValueMap.add("flow", flow.getFlowId());
			linkedMultiValueMap.add("cronExpression", project.getCronExpression());
			String postForObject = restTemplate.postForObject(serverAddress + "/schedule", linkedMultiValueMap, String.class);
			Map map=JSONObject.parseObject(postForObject, Map.class);
			if(!"success".equals(map.get("status"))){
				error++;
				message=(String) map.get("error");
				continue;
			}
			success++;
		}
		return new ResponseData.Builder(error==0?"操作成功!":"成功"+success+"个flow，失败"+error+"个flow!"+message).success();

	}
	/**
	 * project是否存在
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public Boolean isExistProject(Project project) throws Exception {
		SSLUtil.turnOffSslChecking();
		HttpHeaders hs = new HttpHeaders();
		hs.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		hs.add("X-Requested-With", "XMLHttpRequest");
		hs.add("Accept", "text/plain;charset=utf-8");
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("ajax", "fetchprojectflows");
		map.put("name", project.getName());
		ResponseEntity<String> exchange = restTemplate.exchange(
				serverAddress + "/manager?ajax=fetchprojectflows&session.id={sessionId}&project={name}", HttpMethod.GET,
				new HttpEntity<String>(hs), String.class, map);
		Map<String, Object> pro = JSONObject.parseObject(exchange.getBody(), Map.class);
		return pro.get("projectId")!=null;
	}
	/**
	 * 一个project下面的任务流
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public List<Flow> getProjectFlows(Project project) throws Exception {
		SSLUtil.turnOffSslChecking();
		HttpHeaders hs = new HttpHeaders();
		hs.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		hs.add("X-Requested-With", "XMLHttpRequest");
		hs.add("Accept", "text/plain;charset=utf-8");
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("ajax", "fetchprojectflows");
		map.put("name", project.getName());
		ResponseEntity<String> exchange = restTemplate.exchange(
				serverAddress + "/manager?ajax=fetchprojectflows&session.id={sessionId}&project={name}", HttpMethod.GET,
				new HttpEntity<String>(hs), String.class, map);
		Map<String,Object> pro = JSONObject.parseObject(exchange.getBody(), Map.class);
		return JSONArray.parseArray(JSONObject.toJSONString(pro.get("flows")), Flow.class);
	}
	/**
	 * 调动一个project的Flows可以多个flow
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public ResponseData<String> scheduleProject(Project project) throws Exception {
		if(!isExistProject(project)){
			return new ResponseData.Builder("project："+project.getName()+"不存在！").error("project："+project.getName()+"不存在！");
		}
		if(project.getFlows()==null||project.getFlows().size()==0){
			return new ResponseData.Builder("请为  project："+project.getName()+"指定flow Id ！").error("请为  project："+project.getName()+"指定flow Id ！");
		}
		SSLUtil.turnOffSslChecking();
		int success=0;
		int error=0;
		LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<String, Object>();
		linkedMultiValueMap.add("session.id", sessionId);
		linkedMultiValueMap.add("ajax", "scheduleCronFlow");
		linkedMultiValueMap.add("projectName", project.getName());
		for(Flow flow:project.getFlows()){
			linkedMultiValueMap.add("flow", flow.getFlowId());
			linkedMultiValueMap.add("cronExpression", flow.getCronExpression());
			String postForObject = restTemplate.postForObject(serverAddress + "/schedule", linkedMultiValueMap, String.class);
			if(!"success".equals(JSONObject.parseObject(postForObject, Map.class).get("status"))){
				error++;
				continue;
			}
			success++;
		}
        return new ResponseData.Builder(error==0?"操作成功!":"成功"+success+"个flow，失败"+error+"个flow!").success();
	}
	/**
	 * 使用说明，
	 * 1、在没有zip包时，直接创建一个工程，不调度，返回成功
	 * 2、存在project时，不会更新，返回失败
	 * 3、调度cron表达式取project的cron表达式，将为project下所有flow采用这个cron表达式
	 * 创建并调度一个project
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public ResponseData<String> createAndScheduleProject(Project project) throws Exception {
		//判断是否存在
		if(isExistProject(project)){
			return new ResponseData.Builder("project："+project.getName()+"已经存在！").error("project："+project.getName()+"已经存在！");
		}
		//创建project
		Integer createResult=createProject(project).getStatus();
		if(ResponseData.Status.SUCCESS.getCode()!=createResult){
			return new ResponseData.Builder("project："+project.getName()+"创建不成功！").error("project："+project.getName()+"创建不成功！");
		}
		//上传zip
		createResult = uploadZip(project.getZipFileName(), project.getName()).getStatus();
		if(ResponseData.Status.SUCCESS.getCode()!=createResult){
			return new ResponseData.Builder("project："+project.getName()+"的zip包："+project.getZipFileName()+"上传不成功！").error("project："+project.getName()+"的zip包："+project.getZipFileName()+"上传不成功！");
		}

		//查询project的flows
		project.setFlows(getProjectFlows(project));
		//如果没有flows 就创建project 不调度
		if(project.getFlows()==null||project.getFlows().size()==0){
			return new ResponseData.Builder("project创建成功，没有flow调度").success("project创建成功，没有flow调度");
		}
		//如果没有flows 就创建project 不调度
		if(project.getCronExpression()==null){
			return new ResponseData.Builder("project创建成功，cron表达式为空！").success("project创建成功，cron表达式为空！");
		}
		SSLUtil.turnOffSslChecking();
		int success=0;
		int error=0;
		String message="";
		LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<String, Object>();
		linkedMultiValueMap.add("session.id", sessionId);
		linkedMultiValueMap.add("ajax", "scheduleCronFlow");
		linkedMultiValueMap.add("projectName", project.getName());
		for(Flow flow:project.getFlows()){
			linkedMultiValueMap.add("flow", flow.getFlowId());
			linkedMultiValueMap.add("cronExpression", project.getCronExpression());
			String postForObject = restTemplate.postForObject(serverAddress + "/schedule", linkedMultiValueMap, String.class);
			Map map=JSONObject.parseObject(postForObject, Map.class);
			if(!"success".equals(map.get("status"))){
				error++;
				message=(String) map.get("error");
				continue;
			}
			success++;
		}
		return new ResponseData.Builder(error==0?"操作成功!":"成功"+success+"个flow，失败"+error+"个flow!"+message).success();
	}

	/**
	 * 删除一个project
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public ResponseData<String> deleteProject(Project project) throws Exception {
		if(!isExistProject(project)){
			return new ResponseData.Builder("project："+project.getName()+"不存在！").error("project："+project.getName()+"不存在！");
		}
		SSLUtil.turnOffSslChecking();
		HttpHeaders hs = new HttpHeaders();
		hs.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		hs.add("X-Requested-With", "XMLHttpRequest");
		hs.add("Accept", "text/plain;charset=utf-8");
		Map<String, String> map = BeanUtils.describe(project);
		map.put("sessionId", sessionId);
		ResponseEntity<String> exchange = restTemplate.exchange(
				serverAddress + "/manager?session.id={sessionId}&delete=true&project={name}", HttpMethod.GET,
				new HttpEntity<String>(hs), String.class, map);
		return new ResponseData.Builder(exchange.getBody()).success();
	}

	/**
	 * 上传zip包
	 * @param zipFileName
	 * @param projectName
	 * @return
	 * @throws Exception
	 */
	public ResponseData<String> uploadZip(String zipFileName,String projectName) throws Exception {
		if(!isExistProject(new Project(projectName,null,null))){
			return new ResponseData.Builder("project："+projectName+"不存在！").error("project："+projectName+"不存在！");
		}
		SSLUtil.turnOffSslChecking();
		FileSystemResource resource = new FileSystemResource(new File(zipFilePath+File.separator+zipFileName));
        LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<String, Object>();
        linkedMultiValueMap.add("session.id", sessionId);
        linkedMultiValueMap.add("ajax", "upload");
        linkedMultiValueMap.add("project", projectName);
        linkedMultiValueMap.add("file", resource);
        String postForObject = restTemplate.postForObject(serverAddress + "/manager", linkedMultiValueMap, String.class);
        return new ResponseData.Builder(postForObject).success();
	}

	public static void main(String[] args) throws Exception{
		AzkabanJobManageService service=new AzkabanJobManageService();
		//登录
		//System.out.println(service.login("azkaban", "azkaban").getData());
		//创建project
		//System.out.println(service.createProject(new Project("PPpro_ject-Test测试创建","测试创建","create")).getData());
		//删除project
		//System.out.println(service.deleteProject(new Project("ystest0101","测试创建","create")).getData());
		//System.out.println(service.uploadZip("azkaban.zip","PPpro_ject-Test").getData());
		//System.out.println(service.isExistProject(new Project("PPpro_ject-Test","测试创建","create")));
		/*for(Flow f:service.getProjectFlows(new Project("jobETL","测试创建","create"))){

			System.out.println(f.getFlowId());
		}*/


		/*Project project = new Project("PPpro_ject-Test","测试创建","create");
		Flow flow=new Flow();
		flow.setFlowId("end");
		flow.setCronExpression("0 1/5 * ? * *");
		List<Flow> flows=new ArrayList<>();
		flows.add(flow);
		project.setFlows(flows);
		System.out.println(service.scheduleProject(project).getData());*/

		//测试创建并调度project
		Project project = new Project("PPpro_ject_create_and_schedule_测试创建并且调度","测试创建并且调度","create");
		project.setZipFileName("azkaban.zip");
		project.setCronExpression("0 1/5 * ? * *");
		System.out.println(service.createAndScheduleProject(project).getData());
	}
}

