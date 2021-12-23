package com.pig4cloud.pig.admin.common.ssoutil;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.pig4cloud.pig.admin.model.SoapEntity;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import sun.misc.BASE64Encoder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @ClassName WebServiceHttpClient
 * @Author Duys
 * @Description
 * @Date 2021/12/10 14:31
 **/
public class WebServiceHttpClient {

	public static JSONObject post(SoapEntity soapEntity) {

		try {
			URL url = new URL(soapEntity.getWdslUrl());
			// 1 建立连接，并将连接强转为Http连接
			URLConnection conn = url.openConnection();
			HttpURLConnection con = (HttpURLConnection) conn;
			// 2，设置请求方式和请求头：
			con.setDoInput(true); // 是否有入参
			con.setDoOutput(true); // 是否有出参
			con.setRequestMethod("POST"); // 设置请求方式
			con.setRequestProperty("content-type", "application/soap+xml; charset=utf-8");
			con.setRequestProperty("Host", soapEntity.getHost());
			//conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			//conn.setRequestProperty("SOAPAction", "http://Centralism.WebService/GetUserRoles");
			// 3，通过流的方式将请求体发送出去：
			OutputStream out = con.getOutputStream();
			soapEntity.getSoapMessage().writeTo(out);
			InputStream inputStream = con.getInputStream();
			String res = IOUtils.toString(inputStream, "UTF-8");
			System.out.println("res -> " + res);
			JSONObject jsonObject = UserWebServiceResponse.xmlToJson(res, soapEntity.getType());
			return jsonObject;
		} catch (Exception e) {

		}
		return null;
	}

	// f524b714-0ac6-459c-acfe-d5042b10f6b5
	// http://address/api/SSOService/GetUser?token=xxx
	public static void main(String[] args) throws Exception {
		String token = "c6d6a561-6a98-41c0-a97b-55f05d648836";
		String appName = "授权管理系统";
		String appCode = "Centralism";
		String usrl = "http://192.168.0.147:9011/sso/api/SSOService/GetUser?token=" + token;

		URL url = new URL(usrl);
		URLConnection conn = url.openConnection();
		HttpURLConnection con = (HttpURLConnection) conn;
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("token", token);
		//header = Base64(AppName)|TimeStamp|Sign
		//Sign= MD5(Base64(AppName)|AppCode|TimeStamp|Token)
		BASE64Encoder encoder = new BASE64Encoder();
		byte[] textByte = appName.getBytes(StandardCharsets.UTF_8);
		String base64AppName = encoder.encode(textByte);

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
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic " + header);
		con.disconnect();
		InputStream inputStream = con.getInputStream();
		String res = IOUtils.toString(inputStream, "UTF-8");
		System.out.println(res);
		// 7d341e64-192e-452f-81ed-65cc4f15e7f2
	}
}
