package com.pig4cloud.pig.admin.sso.common.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName SSOTypeEnum
 * @Author Duys
 * @Date 2023/11/8 14:17
 */
public enum SSOTypeEnum {
	SOAP_1_1(1, "soap1.1"),
	SOAP_1_2(2, "soap1.2"),
	;


	private final Integer code;
	private final String desc;

	SSOTypeEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	private final static Map<Integer, SSOTypeEnum> BY_CODE_MAP =
			Arrays.stream(SSOTypeEnum.values())
					.collect(Collectors.toMap(SSOTypeEnum::getCode, type -> type));

	private final static Map<String, SSOTypeEnum> BY_NAME_MAP
			= Arrays.stream(SSOTypeEnum.values())
			.collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));


	/**
	 * 获取枚举对象
	 *
	 * @param code 代码
	 * @return 根据编码转换出来的枚举对象
	 */
	public static SSOTypeEnum parse(Integer code) {
		return BY_CODE_MAP.get(code);
	}

	/**
	 * 获取枚举对象
	 *
	 * @param name 名字
	 * @return 根据枚举名转换出来的枚举对象
	 */
	public static SSOTypeEnum parse(String name) {
		return BY_NAME_MAP.get(name.trim().toLowerCase());
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
