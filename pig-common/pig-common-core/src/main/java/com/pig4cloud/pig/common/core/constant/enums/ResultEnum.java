package com.pig4cloud.pig.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础应答码
 * @author xiong
 */
@AllArgsConstructor
@Getter
public enum ResultEnum implements ResultEnumBase {

    /**
     * 返回码
     */
    SUCCESS(1, "SUCCESS"),
    SYSTEM_ERROR(-999, "系统错误"),
    PARAM_ERROR(-102, "参数错误"),
    ILLEGAL_OPERATION_ERROR(-103, "非法操作"),
    REQUEST_REPEAT(-106, "请勿重复提交请求"),
    ;
    private final Integer code;
    private final String msg;


}