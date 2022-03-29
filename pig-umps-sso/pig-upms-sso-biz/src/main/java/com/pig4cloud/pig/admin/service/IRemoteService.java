package com.pig4cloud.pig.admin.service;

import com.pig4cloud.pig.admin.model.SSOPrivilege;
import com.pig4cloud.pig.admin.model.SSORoleInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName IRemoteService
 * @Author Duys
 * @Description 远端
 * @Date 2021/12/9 10:42
 **/

public interface IRemoteService {

	List<SSORoleInfo> getSSORoleInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);

	List<SSOPrivilege> getSSOPrivilege(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);

	List<SSOPrivilege> getSSOMenus(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);
}
