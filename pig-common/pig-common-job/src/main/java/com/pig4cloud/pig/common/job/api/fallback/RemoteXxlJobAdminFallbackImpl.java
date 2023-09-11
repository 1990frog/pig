package com.pig4cloud.pig.common.job.api.fallback;

import com.pig4cloud.pig.common.job.api.RemoteXxlJobAdminService;
import com.pig4cloud.pig.common.job.api.XxlJobInfo;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>
 * xxl-job-admin
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/11/7
 */
@Slf4j
@Component
public class RemoteXxlJobAdminFallbackImpl implements RemoteXxlJobAdminService {

	@Setter
	private Throwable cause;

	@Override
	public ReturnT<String> add(XxlJobInfo jobInfo) {
		log.error("xxl-job-admin 添加任务失败", cause);
		return null;
	}

	@Override
	public ReturnT<String> update(XxlJobInfo jobInfo) {
		log.error("xxl-job-admin 更新任务失败", cause);
		return null;
	}

	@Override
	public ReturnT<String> remove(int id) {
		log.error("xxl-job-admin 删除任务失败", cause);
		return null;
	}

	@Override
	public ReturnT<String> pause(Integer id) {
		log.error("xxl-job-admin 暂停任务失败", cause);
		return null;
	}

	@Override
	public ReturnT<String> start(Integer id) {
		log.error("xxl-job-admin 开始任务失败", cause);
		return null;
	}

	@Override
	public ReturnT<String> triggerJob(int id, String executorParam, String addressList) {
		log.error("xxl-job-admin 触发任务失败", cause);
		return null;
	}
}
