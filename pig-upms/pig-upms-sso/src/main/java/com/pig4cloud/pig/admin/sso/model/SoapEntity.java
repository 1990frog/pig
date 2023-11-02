package com.pig4cloud.pig.admin.sso.model;

import com.pig4cloud.pig.admin.sso.common.enums.SoapTypeEnum;
import lombok.Data;

import javax.xml.soap.SOAPMessage;

/**
 * @ClassName SoapEntity
 * @Author Duys
 * @Description
 * @Date 2021/12/10 15:00
 **/
@Data
public class SoapEntity {
	private String host;
	private SoapTypeEnum type;
	private String token;
	private String appCode;
	private String appName;
	private String userCode;
	private String userName;
	private Long current;
	private Long size;
	private boolean isAscending;
	private Long timeStamp;
	private String wdslUrl;
	private String sign;
	private SOAPMessage soapMessage;
}
