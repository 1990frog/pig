package com.pig4cloud.pig.common.core.constant.enums;

/**
 * 返回码枚举接口，所有业务返回码枚举必须继承
 * 
 * @author xiong
 */
public interface ResultEnumBase {

    /**
     * code
     */
    Integer getCode();

    /**
     * msg
     */
    String getMsg();

}
