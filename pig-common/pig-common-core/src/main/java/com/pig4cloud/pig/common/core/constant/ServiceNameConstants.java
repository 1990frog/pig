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
 * @date 2018年06月22日16:41:01 服务名称
 */
public interface ServiceNameConstants {

	/**
	 * 认证服务的SERVICEID
	 */
	String AUTH_SERVICE = "pig-auth";

	/**
	 * UMPS模块
	 */
	String UMPS_SERVICE = "pig-upms-biz";

	/**
	 * xxl_job_admin
	 */
	String XXL_JOB_ADMIN = "pig-xxl-job-admin";

	/**
	 * 数据核查：控制端
	 */
	String DQ_MASTER = "dq-master-biz";

	/**
	 * 数据核查：运行端
	 */
	String DQ_WORKER = "dq-worker";

	/**
	 * 数据核查：国家类上报
	 */
	String DQ_REPORT = "dq-report-biz";

	/**
	 * 数据核查：文件管理服务
	 */
	String DQ_FILEMANAGER = "dq-filemanager-biz";


}
