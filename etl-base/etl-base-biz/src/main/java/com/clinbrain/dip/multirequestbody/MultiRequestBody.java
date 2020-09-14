package com.clinbrain.dip.multirequestbody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Liaopan on 2020-09-10.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiRequestBody {
	/**
	 * 是否必须出现的参数
	 */
	boolean required() default true;

	/**
	 * 当value的值或者参数名不匹配时，是否允许解析最外层属性到该对象
	 */
	boolean parseAllFields() default true;

	/**
	 * 解析时用到的JSON的key
	 */
	String value() default "";
}
