package com.clinbrain.dip.rest.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

public class SessionUtil {
    public final static String ENVIRONMENT_CODE="environmentCode";
    public static HttpSession getSession(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    }
    public static Object getSessionAttr(String key,Object defaultValue){
        HttpSession session=getSession();
        Object obj = getSession().getAttribute(key);
        return obj==null?defaultValue:obj;
    }
}
