package com.pig4cloud.pig.common.job.api;

import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.job.api.fallback.RemoteXxlJobAdminServiceFallbackImpl;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "remoteXxlJobAdminService", value = ServiceNameConstants.XXL_JOB_ADMIN
		, fallbackFactory = RemoteXxlJobAdminServiceFallbackImpl.class)
public interface RemoteXxlJobAdminService {

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> add(XxlJobInfo jobInfo);

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> update(XxlJobInfo jobInfo);

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> remove(@RequestParam("id") int id);

	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> pause(@RequestParam("id") Integer id);

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> start(@RequestParam("id") Integer id);

	@RequestMapping(value = "/trigger", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> triggerJob(@RequestParam("id") int id, @RequestParam("executorParam") String executorParam, @RequestParam("addressList") String addressList);

}
