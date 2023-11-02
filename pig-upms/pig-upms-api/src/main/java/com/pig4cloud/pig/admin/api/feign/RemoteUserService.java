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

package com.pig4cloud.pig.admin.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.entity.SysUser;
import com.pig4cloud.pig.admin.api.entity.UserExtendInfo;
import com.pig4cloud.pig.admin.api.feign.factory.RemoteUserServiceFallbackFactory;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author lengleng
 * @date 2019/2/1
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.UMPS_SERVICE,
		fallbackFactory = RemoteUserServiceFallbackFactory.class)
public interface RemoteUserService {

	/**
	 * 通过用户名查询用户、角色信息
	 *
	 * @param username 用户名
	 * @param from     调用标志
	 * @return R
	 */
	@Deprecated
	@GetMapping("/user/info/{username}")
	R<UserInfo> info(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 通过用户名查询用户、角色信息
	 *
	 * @param username 用户名
	 * @param sysCode  系统标识
	 * @param from     调用标志
	 * @return R
	 */
	@GetMapping("/user/info/{username}/{sysCode}")
	R<UserInfo> infoNew(@PathVariable("username") String username, @PathVariable("sysCode") String sysCode, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 通过社交账号查询用户、角色信息
	 *
	 * @param inStr appid@code
	 * @return
	 */
	@GetMapping("/social/info/{inStr}")
	R<UserInfo> social(@PathVariable("inStr") String inStr);


	@Deprecated
	@GetMapping({"/inner/user/token/info"})
	R<SysUser> currentUserInfo(@RequestHeader(SecurityConstants.FROM) String var1, @RequestParam(name = "token") String token);

	@GetMapping("/user/sys/list")
	List<SysUser> sysUserList(@RequestParam("sysClass") String sysClass);

	/**
	 * @param current
	 * @param size
	 * @param sysClass
	 * @param keyword
	 * @return 指定系统用户分页
	 */
	@GetMapping("/user/sys/page")
	Page<SysUser> sysUserPage(@RequestParam("current") Long current,
							  @RequestParam("size") Long size,
							  @RequestParam("sysClass") String sysClass,
							  @RequestParam(value = "keyword", required = false) String keyword);

	@GetMapping("/user/extend/page")
	Page<UserExtendInfo> userPage(@RequestParam("current") Long current,
								  @RequestParam("size") Long size,
								  @RequestParam(value = "keyword", required = false) String keyword,
								  @RequestHeader(SecurityConstants.FROM) String from);
}
