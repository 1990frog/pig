package com.pig4cloud.pig.admin.sso.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pig4cloud.pig.admin.api.entity.UserExtendInfo;
import com.pig4cloud.pig.admin.sso.model.SSOPrivilege;
import com.pig4cloud.pig.admin.sso.model.SSORoleDTO;
import com.pig4cloud.pig.admin.sso.model.SSORoleInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName IRemoteService
 * @Author Duys
 * @Description 远端
 * @Date 2021/12/9 10:42
 **/

public interface IRemoteService {

	SSORoleDTO getSSORoleInfo(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);

	List<SSOPrivilege> getSSOPrivilege(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);

	List<SSOPrivilege> getSSOMenus(String serverToken, Map<String, String> serverInfoMap, Map ssoClientInfo);

	Integer findUserCount(String userName,String serverToken, Map ssoClientInfo);

	List<UserExtendInfo> findUserInfo(String userName,String serverToken, Long current, Long size, Map ssoClientInfo);

	IPage<UserExtendInfo> findUserInfo(Long current, Long size, String serverToken, String keyword, Map ssoClientInfo);
}
