package com.clinbrain.dip.multirequestbody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Liaopan on 2020-09-10.
 */
public class MultiRequestBodyArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String JSONBODY_ATTRIBUTE = "JSON_REQUEST_BODY";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(MultiRequestBody.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String jsonBody = getRequestBody(webRequest);

		JSONObject jsonObject = JSON.parseObject(jsonBody);
		MultiRequestBody parameterAnnotation = parameter.getParameterAnnotation(MultiRequestBody.class);
		String key = parameterAnnotation.value();
		Object value ;
		if(StringUtils.isNotEmpty(key)) {
			value =jsonObject.get(key);
			if(value == null && parameterAnnotation.required()) {
				throw new IllegalArgumentException(String.format("required param %s is not present", key));
			}
		} else {
			key = parameter.getParameterName();
			value = jsonObject.get(key);
		}

		Class<?> parameterType = parameter.getParameterType();
		// 通过注解的value或者参数名解析，能拿到value进行解析
		if (value != null) {
			//基本类型
			if (parameterType.isPrimitive()) {
				return parsePrimitive(parameterType.getName(), value);
			}
			// 基本类型包装类
			if (isBasicDataTypes(parameterType)) {
				return parseBasicTypeWrapper(parameterType, value);
				// 字符串类型
			} else if (parameterType == String.class) {
				return value.toString();
			}
			// 其他复杂对象
			return JSON.parseObject(value.toString(), parameterType);
		}

		// 解析不到则将整个json串解析为当前参数类型
		if (isBasicDataTypes(parameterType)) {
			if (parameterAnnotation.required()) {
				throw new IllegalArgumentException(String.format("required param %s is not present", key));
			} else {
				return null;
			}
		}

		// 非基本类型，不允许解析所有字段，必备参数则报错，非必备参数则返回null
		if (!parameterAnnotation.parseAllFields()) {
			// 如果是必传参数抛异常
			if (parameterAnnotation.required()) {
				throw new IllegalArgumentException(String.format("required param %s is not present", key));
			}
			// 否则返回null
			return null;
		}
		// 非基本类型，允许解析，将外层属性解析
		Object result;
		try {
			result = JSON.parseObject(jsonObject.toString(), parameterType);
		} catch (JSONException jsonException) {
			result = null;
		}

		// 如果非必要参数直接返回，否则如果没有一个属性有值则报错
		if (!parameterAnnotation.required()) {
			return result;
		} else {
			boolean haveValue = false;
			Field[] declaredFields = parameterType.getDeclaredFields();
			for (Field field : declaredFields) {
				field.setAccessible(true);
				if (field.get(result) != null) {
					haveValue = true;
					break;
				}
			}
			if (!haveValue) {
				throw new IllegalArgumentException(String.format("required param %s is not present", key));
			}
			return result;
		}
	}

	/**
	 * 基本类型解析
	 */
	private Object parsePrimitive(String parameterTypeName, Object value) {
		final String booleanTypeName = "boolean";
		if (booleanTypeName.equals(parameterTypeName)) {
			return Boolean.valueOf(value.toString());
		}
		final String intTypeName = "int";
		if (intTypeName.equals(parameterTypeName)) {
			return Integer.valueOf(value.toString());
		}
		final String charTypeName = "char";
		if (charTypeName.equals(parameterTypeName)) {
			return value.toString().charAt(0);
		}
		final String shortTypeName = "short";
		if (shortTypeName.equals(parameterTypeName)) {
			return Short.valueOf(value.toString());
		}
		final String longTypeName = "long";
		if (longTypeName.equals(parameterTypeName)) {
			return Long.valueOf(value.toString());
		}
		final String floatTypeName = "float";
		if (floatTypeName.equals(parameterTypeName)) {
			return Float.valueOf(value.toString());
		}
		final String doubleTypeName = "double";
		if (doubleTypeName.equals(parameterTypeName)) {
			return Double.valueOf(value.toString());
		}
		final String byteTypeName = "byte";
		if (byteTypeName.equals(parameterTypeName)) {
			return Byte.valueOf(value.toString());
		}
		return null;
	}

	/**
	 * 基本类型包装类解析
	 */
	private Object parseBasicTypeWrapper(Class<?> parameterType, Object value) {
		if (Number.class.isAssignableFrom(parameterType)) {
			Number number = (Number) value;
			if (parameterType == Integer.class) {
				return number.intValue();
			} else if (parameterType == Short.class) {
				return number.shortValue();
			} else if (parameterType == Long.class) {
				return number.longValue();
			} else if (parameterType == Float.class) {
				return number.floatValue();
			} else if (parameterType == Double.class) {
				return number.doubleValue();
			} else if (parameterType == Byte.class) {
				return number.byteValue();
			}
		} else if (parameterType == Boolean.class) {
			return value.toString();
		} else if (parameterType == Character.class) {
			return value.toString().charAt(0);
		}
		return null;
	}

	/**
	 * 判断是否为基本数据类型包装类
	 */
	private boolean isBasicDataTypes(Class clazz) {
		Set<Class> classSet = new HashSet<>();
		classSet.add(Integer.class);
		classSet.add(Long.class);
		classSet.add(Short.class);
		classSet.add(Float.class);
		classSet.add(Double.class);
		classSet.add(Boolean.class);
		classSet.add(Byte.class);
		classSet.add(Character.class);
		return classSet.contains(clazz);
	}

	private String getRequestBody(NativeWebRequest webRequest) {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		// 有就直接获取
		String jsonBody = (String) webRequest.getAttribute(JSONBODY_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
		// 没有就从请求中读取
		if (jsonBody == null) {
			try {
				jsonBody = IOUtils.toString(servletRequest.getReader());
				webRequest.setAttribute(JSONBODY_ATTRIBUTE, jsonBody, NativeWebRequest.SCOPE_REQUEST);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return jsonBody;
	}
}
