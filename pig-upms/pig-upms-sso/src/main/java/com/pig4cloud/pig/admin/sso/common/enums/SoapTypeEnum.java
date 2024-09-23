package com.pig4cloud.pig.admin.sso.common.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SoapTypeEnum {

	SOAP_ROLE(1, "请求角色"),
	SOAP_PER(2, "请求权限"),
	SOAP_ORG(3, "请求组织"),

	SOAP_USER_PAGE(4, "请求用户分页信息"),

	SOAP_USER_PAGE_TOTAL(5, "请求用户总数"),

	SOAP_ALL_ROLE(6, "请求所有的角色信息"),
	;


	private final Integer code;
	private final String desc;

	SoapTypeEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	private final static Map<Integer, SoapTypeEnum> BY_CODE_MAP =
			Arrays.stream(SoapTypeEnum.values())
					.collect(Collectors.toMap(SoapTypeEnum::getCode, type -> type));

	private final static Map<String, SoapTypeEnum> BY_NAME_MAP
			= Arrays.stream(SoapTypeEnum.values())
			.collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));


	/**
	 * 获取枚举对象
	 *
	 * @param code 代码
	 * @return 根据编码转换出来的枚举对象
	 */
	public static SoapTypeEnum parse(Integer code) {
		return BY_CODE_MAP.get(code);
	}

	/**
	 * 获取枚举对象
	 *
	 * @param name 名字
	 * @return 根据枚举名转换出来的枚举对象
	 */
	public static SoapTypeEnum parse(String name) {
		return BY_NAME_MAP.get(name.trim().toLowerCase());
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
