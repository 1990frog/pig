package com.pig4cloud.pig.common.job.api;

import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${xxl.job.admin.addresses:http://pig-xxl-job-admin}")
public interface RemoteXxlJobAdminService {

	@RequestMapping(value = "/xxl-job-admin/jobinfo/add", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> add(@SpringQueryMap XxlJobInfo jobInfo);

	@RequestMapping(value = "/xxl-job-admin/jobinfo/update", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> update(@SpringQueryMap XxlJobInfo jobInfo);

	@RequestMapping(value = "/xxl-job-admin/jobinfo/remove", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> remove(@RequestParam("id") int id);

	@RequestMapping(value = "/xxl-job-admin/jobinfo/stop", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> pause(@RequestParam("id") Integer id);

	@RequestMapping(value = "/xxl-job-admin/jobinfo/start", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> start(@RequestParam("id") Integer id);

	@RequestMapping(value = "/xxl-job-admin/jobinfo/trigger", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> triggerJob(@RequestParam("id") int id, @RequestParam("executorParam") String executorParam, @RequestParam("addressList") String addressList);

}
