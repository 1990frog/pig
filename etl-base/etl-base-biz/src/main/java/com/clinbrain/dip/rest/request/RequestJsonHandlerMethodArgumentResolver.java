package com.clinbrain.dip.rest.request;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * @author lianglele
 * @date 2020-10-19 10:53
 */
public class RequestJsonHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestJson.class);
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest,
								  WebDataBinderFactory webDataBinderFactory) throws Exception {
		RequestJson requestJson = parameter.getParameterAnnotation(RequestJson.class);
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		BufferedReader reader = request.getReader();
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];
		int rd;
		while ((rd = reader.read(buf)) != -1) {
			sb.append(buf, 0, rd);
		}
		String value = requestJson.value();
//		if(value.equals("moduleTask")){
//			ModuleTaskRequest jsonObject = JSONObject.parseObject(sb.toString(),ModuleTaskRequest.class);
//			return jsonObject;
//		} else {
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return objectMapper.readValue(sb.toString(), ModuleTaskRequest.class);
//		}

	}
}
