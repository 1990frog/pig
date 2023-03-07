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

package com.pig4cloud.pig.admin.api.feign.fallback;

import com.pig4cloud.pig.admin.api.feign.RemoteMenuService;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.common.core.util.R;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/11/16
 */
@Slf4j
@Component
public class RemoteMenuServiceFallbackImpl implements RemoteMenuService {

	@Setter
	private Throwable cause;

	@Override
	public List<MenuVO> findMenuBySystem(String system) {
		log.error("feign 查询菜单信息失败:system={}, error = {}", system, cause);
		return null;
	}
}
