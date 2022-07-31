package com.pig4cloud.pig.common.job.api;

import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.job.api.fallback.RemoteXxlJobAdminServiceFallbackImpl;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(contextId = "remoteXxlJobAdminService", value = ServiceNameConstants.XXL_JOB_ADMIN
		, fallbackFactory = RemoteXxlJobAdminServiceFallbackImpl.class)
public interface RemoteXxlJobAdminService {

	@PostMapping("/pig-xxl-job/jobinfo/add")
	@ResponseBody
	ReturnT<String> add(XxlJobInfo jobInfo);

	@RequestMapping("/pig-xxl-job/jobinfo/update")
	@ResponseBody
	ReturnT<String> update(XxlJobInfo jobInfo);

	@RequestMapping("/pig-xxl-job/jobinfo/stop")
	@ResponseBody
	ReturnT<String> pause(int id);

	@RequestMapping("/pig-xxl-job/jobinfo/start")
	@ResponseBody
	ReturnT<String> start(int id);
}
