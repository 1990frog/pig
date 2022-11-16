package com.pig4cloud.pig.common.job.api;

import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.job.api.factory.RemoteXxlJobAdminFallbackFactory;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * xxl-job-admin 接口
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/11/7
 */
//@FeignClient(name = "${xxl.job.admin.addresses:http://pig-xxl-job-admin}")
@FeignClient(contextId = "remoteXxlJobAdminService", value = ServiceNameConstants.XXL_JOB_ADMIN,
		fallbackFactory = RemoteXxlJobAdminFallbackFactory.class)
public interface RemoteXxlJobAdminService {

	/**
	 *
	 * @param jobInfo
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/add", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> add(@SpringQueryMap XxlJobInfo jobInfo);

	/**
	 *
	 * @param jobInfo
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/update", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> update(@SpringQueryMap XxlJobInfo jobInfo);

	/**
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/remove", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> remove(@RequestParam("id") int id);

	/**
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/stop", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> pause(@RequestParam("id") Integer id);

	/**
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/start", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> start(@RequestParam("id") Integer id);

	/**
	 *
	 * @param id
	 * @param executorParam
	 * @param addressList
	 * @return
	 */
	@RequestMapping(value = "/xxl-job-admin/jobinfo/trigger", method = RequestMethod.POST)
	@ResponseBody
	ReturnT<String> triggerJob(@RequestParam("id") int id, @RequestParam("executorParam") String executorParam, @RequestParam("addressList") String addressList);

}
