package com.pig4cloud.pig.admin.sso.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SSOTypeEnum;
import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import com.pig4cloud.pig.admin.sso.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserRoleInfoParse;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserWebServiceRequest;
import com.pig4cloud.pig.admin.sso.common.ssoutil.UserWebServiceResponse;
import com.pig4cloud.pig.admin.sso.common.ssoutil.WebServiceHttpClient;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.model.SSORoleInfo;
import com.pig4cloud.pig.admin.sso.model.SoapEntity;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import org.springframework.cache.Cache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @ClassName SSOTestModel
 * @Author Duys
 * @Date 2023/11/8 11:18
 */
public class SSOTestModel {
	public static void main(String[] args) {
		//getUser();
		//testUserTotal();
		//testUserRoleOld();
		//testUserRole();
		//testUserPer();
		//testUserPage();
		System.out.println("--------");
		//testUserPageOld();
		//testAppAll();
		//testAppRoleAll();
		//testAppPerAll();
		testUserPage();
	}

	public static void testUserPageOld() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_2);
		soapEntity.setHost("http://192.168.0.72:9011");
		soapEntity.setWdslUrl("http://192.168.0.72:9011/cws");
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("b0480d6e-e362-406f-a818-811ffaee5d7f");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.get(soapEntity);
		System.out.println(object);
	}

	public static void testUserPage() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.220:10023");
		soapEntity.setWdslUrl("http://192.168.0.220:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE);
		soapEntity.setUserCode("dq");
		soapEntity.setAppName("数据质量核查与分析软件");
		soapEntity.setAppCode("DATA_QUALIT");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		soapEntity.setUserName("测试");
		soapEntity.setToken("75292283-daa3-46a5-a7a4-cc922cb425e4");
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.get4api(soapEntity);
		System.out.println("----------");
		System.out.println(object);
	}

	public static void testUserOrgOld() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_2);
		soapEntity.setHost("http://192.168.0.72:9011");
		soapEntity.setWdslUrl("http://192.168.0.72:9011/cws");
		soapEntity.setType(SoapTypeEnum.SOAP_ORG);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("b0480d6e-e362-406f-a818-811ffaee5d7f");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);

	}

	public static void testUserOrg() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_ORG);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("64fd8deb-38f7-4057-881b-f2b0f6fd6070");
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);
	}

	public static void testUserPerOld() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_2);
		soapEntity.setHost("http://192.168.0.72:9011");
		soapEntity.setWdslUrl("http://192.168.0.72:9011/cws");
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("64fd8deb-38f7-4057-881b-f2b0f6fd6070");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);

	}

	public static void testUserPer() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("64fd8deb-38f7-4057-881b-f2b0f6fd6070");
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);

	}

	private static void getUser() {
		String appName = "ETL数据集成平台";
		String appCode = "ETL";
		String token = "5f262849-b27d-49c7-8ece-342aeca66482";
		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", token);
		//header = Base64(AppName)|TimeStamp|Sign
		//Sign= MD5(Base64(AppName)|AppCode|TimeStamp|Token)
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = Base64.encode(textByte);

		//初始化LocalDateTime对象
		ZoneOffset zoneOffset = ZoneOffset.ofHours(0);
		//初始化LocalDateTime对象
		LocalDateTime localDateTime = LocalDateTime.now();
		long TimeStamp = localDateTime.toEpochSecond(zoneOffset);
		String buffer = base64AppName + "|" + appCode + "|" + TimeStamp + "|" + token;
		String Sign = SecureUtil.md5(buffer);
		String header = base64AppName + "|" + TimeStamp + "|" + Sign;

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + header);
		final HttpEntity<String> entity = new HttpEntity<String>(headers);
		final Map map = restTemplate.exchange("http://192.168.0.230:10023/sso/api/SSOService/GetUser" + "?token=" + token,
				HttpMethod.GET, entity, Map.class).getBody();
		if (map != null && !map.keySet().isEmpty()
				&& StrUtil.isNotBlank(Optional.ofNullable(map.get("Identity")).orElse("").toString())) {
			System.out.println("登录成功");
		}
		JSONObject object = JSONUtil.parseObj(map);
		System.out.println(object);
	}

	public static void testUserRole() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("64fd8deb-38f7-4057-881b-f2b0f6fd6070");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);

	}

	public static void testUserRoleOld() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setHost("http://192.168.0.72:9011");
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_2);
		soapEntity.setWdslUrl("http://192.168.0.72:9011/cws");
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("64fd8deb-38f7-4057-881b-f2b0f6fd6070");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.post(soapEntity);

	}

	public static void testUserTotal() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE_TOTAL);
		soapEntity.setUserCode("sys");
		soapEntity.setAppName("ETL数据集成平台");
		soapEntity.setAppCode("ETL");
		soapEntity.setToken("9f6ae9f6-5431-44ad-9dde-0a1869e9873f");
		soapEntity.setCurrent(1l);
		soapEntity.setSize(20l);
		UserWebServiceRequest.buildMessage(soapEntity);
		JSONObject object = WebServiceHttpClient.get(soapEntity);

		/*String json = "searchXml=<Search UserCode=\"\" UserName=\"\" IsDepth=\"false\" OrgCode=\"\" UserType=\"Normal\" />";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Host", soapEntity.getHost());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity httpEntity = new HttpEntity(headers);
		ResponseEntity<String> exchange = restTemplate.exchange("http://192.168.0.230:10023/UserWebService.asmx/QueryUserCount?searchXml=<Search UserCode=\"\" UserName=\"\" IsDepth=\"false\" OrgCode=\"\" UserType=\"Normal\" />", HttpMethod.GET,
				httpEntity, String.class);
		String res = exchange.getBody();
		System.out.println("sso response = " + res);
		JSONObject jsonObject = UserWebServiceResponse.xmlToJsonByString(res, soapEntity.getType());
		System.out.println("sso 本地解析后 = " + jsonObject);*/
	}

	public static void testAppAll() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		soapEntity.setType(SoapTypeEnum.SOAP_USER_PAGE_TOTAL);


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Host", soapEntity.getHost());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity httpEntity = new HttpEntity(headers);
		ResponseEntity<List<Map<String, Object>>> exchange = restTemplate.exchange("http://192.168.0.230:10023/cm/api/App/all", HttpMethod.GET,
				null, new ParameterizedTypeReference<List<Map<String, Object>>>() {
				});
		List<Map<String, Object>> res = exchange.getBody();
		System.out.println("sso response = " + res);
	}

	private static void testAppRoleAll() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode("ETL");
		soapEntity.setAppName("XX");
		soapEntity.setUserCode("sys");
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_ROLE);
		String wdslUrl = soapEntity.getWdslUrl();
		if (wdslUrl.endsWith("/")) {
			wdslUrl += "cm/api/AppRole/listbyapp/" + "ETL";
		} else wdslUrl += "/cm/api/AppRole/listbyapp/" + "ETL";
		//UserWebServiceRequest.buildMessage(soapEntity);
		soapEntity.setWdslUrl(wdslUrl);
		JSONArray roleInfo = WebServiceHttpClient.getToArray(soapEntity);
		System.out.println(roleInfo);
	}

	private static void testAppPerAll() {
		SoapEntity soapEntity = new SoapEntity();
		soapEntity.setAppCode("ETL");
		soapEntity.setAppName("XX");
		soapEntity.setUserCode("sys");
		soapEntity.setSsoType(SSOTypeEnum.SOAP_1_1);
		soapEntity.setHost("http://192.168.0.230:10023");
		soapEntity.setWdslUrl("http://192.168.0.230:10023");
		// 请求角色
		soapEntity.setType(SoapTypeEnum.SOAP_PER);
		String wdslUrl = soapEntity.getWdslUrl();
		if (wdslUrl.endsWith("/")) {
			wdslUrl += "cm/api/AppPrivilege/all/byapp/" + 22;
		} else wdslUrl += "/cm/api/AppPrivilege/all/byapp/" + 22;
		//UserWebServiceRequest.buildMessage(soapEntity);
		soapEntity.setWdslUrl(wdslUrl);
		JSONArray roleInfo = WebServiceHttpClient.getToArray(soapEntity);
		System.out.println(roleInfo);
	}


}
