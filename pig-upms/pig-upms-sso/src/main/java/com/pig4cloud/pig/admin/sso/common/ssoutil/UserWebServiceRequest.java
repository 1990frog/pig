package com.pig4cloud.pig.admin.sso.common.ssoutil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.pig4cloud.pig.admin.sso.common.constants.SSOWebServiceConstants;
import com.pig4cloud.pig.admin.sso.common.enums.SSOTypeEnum;
import com.pig4cloud.pig.admin.sso.model.SoapEntity;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @ClassName UserWebServiceRequest
 * @Author Duys
 * @Description
 * @Date 2021/12/10 15:05
 **/
@Slf4j
public class UserWebServiceRequest {

	public static void buildMessage(SoapEntity soapEntity) {
		try {
			SOAPMessage soapMessage = null;
			if (SSOTypeEnum.SOAP_1_1.equals(soapEntity.getSsoType())) {
				soapMessage = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage();
			} else {
				soapMessage = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
			}
			soapMessage.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.setPrefix(SSOWebServiceConstants.WEB_SERVICE_PREFIX);
			envelope.removeAttribute("xmlns:env");
			SOAPHeader header = envelope.getHeader();
			if (null == header) {
				header = envelope.addHeader();
			}
			header.setPrefix(SSOWebServiceConstants.WEB_SERVICE_PREFIX);
			QName qName = new QName(SSOWebServiceConstants.WEB_SERVICE_NAMESPACE, SSOWebServiceConstants.WEB_SERVICE_REQUEST_HEADER, "");
			SOAPHeaderElement soapHeaderElement = header.addHeaderElement(qName);
			soapHeaderElement.addChildElement("CurrentOperateUserCode").setValue(StrUtil.isEmpty(soapEntity.getUserCode()) ? "" : soapEntity.getUserCode());
			soapHeaderElement.addChildElement("CurrentAppName").setValue(soapEntity.getAppName());
			soapHeaderElement.addChildElement("CurrentAppCode").setValue(soapEntity.getAppCode());
			soapHeaderElement.addChildElement("Sign").setValue(buildSign(soapEntity));
			soapHeaderElement.addChildElement("TimeStamp").setValue(soapEntity.getTimeStamp() + "");

			SOAPBody body = envelope.getBody();
			body.setPrefix(SSOWebServiceConstants.WEB_SERVICE_PREFIX);
			//QName qName1 = new QName(SSOWebServiceConstants.WEB_SERVICE_NAMESPACE, "GetUserRoles", "");
			buildBodyQName(body, soapEntity);
			log.info("build soap message = {} ", soapMessage);
			soapEntity.setSoapMessage(soapMessage);
		} catch (Exception e) {
			throw new RuntimeException("build soapmessage error", e);
		}

	}

	private static void buildBodyQName(SOAPBody body, SoapEntity soapEntity) throws SOAPException {
		QName qName = null;
		String curWsdlUrl = null;
		switch (soapEntity.getType()) {
			case SOAP_ROLE:
				qName = new QName(SSOWebServiceConstants.WEB_SERVICE_NAMESPACE, SSOWebServiceConstants.WEB_SERVICE_USER_ROLE_REQUEST, "");
				body.addChildElement(qName).addChildElement("userCode").setValue(soapEntity.getUserCode());
				curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_ROLE;
				break;
			case SOAP_PER:
				qName = new QName(SSOWebServiceConstants.WEB_SERVICE_NAMESPACE, SSOWebServiceConstants.WEB_SERVICE_USER_PRIVILEGE_REQUEST, "");
				SOAPElement soapElement = body.addChildElement(qName);
				soapElement.addChildElement("userCode").setValue(soapEntity.getUserCode());
				// 固定写法
				soapElement.addChildElement("privilegeSystemCode").setValue("AppPrivilegeSystem");
				curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_PRIVILEGE;
				break;
			case SOAP_ORG:
				qName = new QName(SSOWebServiceConstants.WEB_SERVICE_NAMESPACE, SSOWebServiceConstants.WEB_SERVICE_USER_ORG_REQUEST, "");
				body.addChildElement(qName).addChildElement("userCode").setValue(soapEntity.getUserCode());
				curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_ORG;
				break;
			case SOAP_USER_PAGE:
				if (soapEntity.getSsoType().equals(SSOTypeEnum.SOAP_1_1)) {
					curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_PAGE_1_1;
					curWsdlUrl += "?pageindex=" + soapEntity.getCurrent();
					curWsdlUrl += "&pagesize=" + soapEntity.getSize();
				} else {
					String search = "<Search UserCode=\"\" UserName=\"%s\" IsDepth=\"false\" OrgCode=\"\" UserType=\"Normal\" />";
					// <Pager CurrentPage="1" PageSize="20" />
					String page = "<Pager CurrentPage=\"%s\" PageSize=\"%s\" />";
					curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_PAGE;
					curWsdlUrl += "?searchXml=" + String.format(search, StrUtil.isEmpty(soapEntity.getUserName()) ? "" : soapEntity.getUserName()) + "&";
					curWsdlUrl += "pagerXml=" + String.format(page, soapEntity.getCurrent(), soapEntity.getSize()) + "&";
					curWsdlUrl += "orderField=Code&";
					curWsdlUrl += "isAscending=false";
				}
				break;
			case SOAP_USER_PAGE_TOTAL:
				String searchTotal = "<Search UserCode=\"\" UserName=\"%s\" IsDepth=\"false\" OrgCode=\"\" UserType=\"Normal\" />";
				// <Pager CurrentPage="1" PageSize="20" />
				curWsdlUrl = SSOWebServiceConstants.WEB_SERVICE_USER_PAGE_TOTAL;
				curWsdlUrl += "?searchXml=" + String.format(searchTotal, StrUtil.isEmpty(soapEntity.getUserName()) ? "" : soapEntity.getUserName());
				break;
		}
		soapEntity.setWdslUrl(soapEntity.getWdslUrl() + curWsdlUrl);
	}

	private static String buildSign(SoapEntity soapEntity) {
		BASE64Encoder encoder = new BASE64Encoder();
		byte[] textByte = soapEntity.getAppName().getBytes(StandardCharsets.UTF_8);
		String base64AppName = encoder.encode(textByte);
		//初始化LocalDateTime对象
		ZoneOffset zoneOffset = ZoneOffset.ofHours(0);
		//初始化LocalDateTime对象
		LocalDateTime localDateTime = LocalDateTime.now();
		long timeStamp = localDateTime.toEpochSecond(zoneOffset);
		soapEntity.setTimeStamp(timeStamp);
		String buffer = base64AppName + "|" + soapEntity.getAppCode() + "|" + timeStamp + "|" + soapEntity.getToken();
		String sign = SecureUtil.md5(buffer);
		return sign;
	}


}
