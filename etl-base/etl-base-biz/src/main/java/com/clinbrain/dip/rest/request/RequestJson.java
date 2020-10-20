package com.clinbrain.dip.rest.request;

import java.lang.annotation.*;

/**
 * @author lianglele
 * @date 2020-10-19 10:52
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestJson {
	String value();
}
