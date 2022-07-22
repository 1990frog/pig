package com.pig4cloud.pig.admin.sso.common.execption;

import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.admin.sso.common.enums.ResponseCodeEnum;

import java.util.Optional;

/**
 * @ClassName NotSupportException
 * @Author Duys
 * @Description
 * @Date 2021/12/15 17:12
 **/
public class SSOBusinessException extends RuntimeException {
	private static final long serialVersionUID = 6912363249855903393L;
	private ResponseCodeEnum responseCode;
	private String message;

	/**
	 * 创建异常
	 *
	 * @param code    异常状态码
	 * @param message 提示信息
	 */
	public SSOBusinessException(final Integer code, String message) {
		super(String.valueOf(code));
		this.responseCode = ResponseCodeEnum.parse(code);
		this.message = StrUtil.isBlank(message) ? responseCode.desc() : message;
	}

	/**
	 * 创建异常
	 *
	 * @param responseCode 异常状态码
	 */
	public SSOBusinessException(final ResponseCodeEnum responseCode) {
		super(String.valueOf(responseCode.code()));
		this.responseCode = responseCode;
		this.message = responseCode.desc();
	}

	/**
	 * 创建异常
	 *
	 * @param message 异常信息
	 */
	public SSOBusinessException(final String message) {
		super(message);
	}

	/**
	 * 创建异常
	 *
	 * @param message 异常信息
	 * @param ex      异常根源
	 */
	public SSOBusinessException(final String message, final Throwable ex) {
		super(message, ex);
	}

	public ResponseCodeEnum getResponseCode() {
		return responseCode;
	}

	@Override
	public String getMessage() {
		return Optional.ofNullable(message).orElse(super.getMessage());
	}
}
