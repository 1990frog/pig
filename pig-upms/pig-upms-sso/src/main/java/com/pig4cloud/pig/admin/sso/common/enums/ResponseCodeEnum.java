package com.pig4cloud.pig.admin.sso.common.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ResponseCodeEnum
 * @Author Duys
 * @Description
 * @Date 2021/12/9 10:42
 **/
public enum ResponseCodeEnum {
	/**
	 * 表示API能正常返回
	 */
	SUCCESS(200, "成功"),

	// 全局错误
	SYSTEM_ERROR(10000, "系统异常"),
	NOT_SUPPORT(10001, "不支持的操作，请参详SSO操作"),
	LOGIN_EXPIRED(10002, "登录过期，请重新登录!"),
	USER_INFO_NOT_EXIST(10003, "用户信息不存在，请重新登录！");


	private final static Map<Integer, ResponseCodeEnum> BY_CODE_MAP =
			Arrays.stream(ResponseCodeEnum.values()).collect(Collectors.toMap(ResponseCodeEnum::code, code -> code));

	private final int code;
	private final String desc;
	private final String template;

	ResponseCodeEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
		this.template = "";
	}

	ResponseCodeEnum(int code, String desc, String template) {
		this.code = code;
		this.desc = desc;
		this.template = template;
	}

	public int code() {
		return this.code;
	}

	public String desc() {
		return this.desc;
	}

	public String template() {
		return this.template;
	}

	/**
	 * @param code 代码
	 * @return 转换出来的状态码
	 */
	public static ResponseCodeEnum parse(Integer code) {
		return BY_CODE_MAP.getOrDefault(code, ResponseCodeEnum.SYSTEM_ERROR);
	}
}
