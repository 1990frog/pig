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

package com.pig4cloud.pig.admin.service.impl;

import com.pig4cloud.pig.admin.api.entity.SysDept;
import com.pig4cloud.pig.admin.api.entity.SysDeptRelation;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.service.SysDeptRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lengleng
 * @since 2019/2/1
 */
@Service
@RequiredArgsConstructor
public class SysDeptRelationServiceImpl implements SysDeptRelationService {

	/**
	 * 维护部门关系
	 *
	 * @param sysDept 部门
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDeptRelation(SysDept sysDept) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 通过ID删除部门关系
	 *
	 * @param id
	 */
	@Override
	public void removeDeptRelationById(Integer id) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	/**
	 * 更新部门关系
	 *
	 * @param relation
	 */
	@Override
	public void updateDeptRelation(SysDeptRelation relation) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

}
