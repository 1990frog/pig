package com.pig4cloud.pig.common.core.exception;

import com.pig4cloud.pig.common.core.constant.enums.ResultEnumBase;

/**
 * @author xiong
 */
public class ServiceException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	/**
	 * 返回码
	 */
	private Integer retCode;
	/**
	 * 返回信息
	 */
	private String retMsg;

	/**
	 * 返回数据
	 */
	private Object data;

	public ServiceException(Integer retCode, String retMsg) {
		this(retCode, retMsg, null);
	}

	public ServiceException(Integer retCode, String retMsg, Object data) {
		super(retCode + ":" + retMsg);
		this.retCode = retCode;
		this.retMsg = retMsg;
		this.data = data;
	}

	public ServiceException(ResultEnumBase resultEnumBase) {
		this(resultEnumBase, null);
	}

	public ServiceException(ResultEnumBase resultEnumBase, Object data) {
		this(resultEnumBase.getCode(), resultEnumBase.getMsg(), data);
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public Integer getRetCode() {
		return retCode;
	}

	public void setRetCode(Integer retCode) {
		this.retCode = retCode;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
