package com.pig4cloud.pig.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName SSOUserFill
 * @Author Duys
 * @Description 填充userInfo
 * @Date 2021/12/13 14:30
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SSOUserInfo {
	String userName() default "";

	String sysClass() default "";
}
