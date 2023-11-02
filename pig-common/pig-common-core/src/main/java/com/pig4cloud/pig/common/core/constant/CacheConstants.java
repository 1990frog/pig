/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.common.core.constant;

/**
 * @author lengleng
 * @date 2020年01月01日
 * <p>
 * 缓存的key 常量
 */
public interface CacheConstants {

	/**
	 * oauth 缓存前缀
	 */
	String PROJECT_OAUTH_ACCESS = "pig_oauth:access:";

	/**
	 * oauth 缓存令牌前缀
	 */
	String PROJECT_OAUTH_TOKEN = "pig_oauth:token:";

	/**
	 * 验证码前缀
	 */
	String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY:";

	/**
	 * 菜单信息缓存
	 */
	String MENU_DETAILS = "menu_details";

	/**
	 * 用户信息缓存
	 */
	String USER_DETAILS = "user_details";

	/**
	 * 字典信息缓存
	 */
	String DICT_DETAILS = "dict_details";

	/**
	 * oauth 客户端信息
	 */
	String CLIENT_DETAILS_KEY = "pig_oauth:client:details";

	/**
	 * 参数缓存
	 */
	String PARAMS_DETAILS = "params_details";

	/**
	 * SSO服务端和客户端的token换取缓存 缓存情况是 服务端的token -> 本地生成的token
	 */
	String SSO_SERVER_LOCAL_TOKEN = "sso_server_local_token:token";
	/**
	 * 本地token -> sso 的token
	 */
	String SSO_LOCAL_SERVER_TOKEN = "sso_local_server_token:token";
	/**
	 * 用户信息和token缓存
	 */
	String SSO_SERVER_INFO = "sso_server_info:token";
	/**
	 * 把ssoClientInfo cache住
	 */
	String SSO_CLIENT_INFO = "sso_client_info";

	String SSO_USER_SERVER_TOKEN = "sso_user_server_token:usercode";

	/**
	 * 存的是serverToken对应的用户信息
	 */
	String SSO_SERVER_TOKEN_USER_CACHE = "sso_client:token";
	/**
	 * userInfo
	 */
	String SSO_LOCAL_USER_INFO_CACHE = "sso_usercode_user_info:usercode";

	/**
	 * 缓存角色信息
	 */
	String SSO_USER_ROLE_INFO = "sso_user_role_info:usercode";

	/**
	 * 缓存权限信息
	 */
	String SSO_USER_PRI_INFO = "sso_user_pri_info:usercode";

	String SSO_CLIENT_ID = "sso_client_id";

}
