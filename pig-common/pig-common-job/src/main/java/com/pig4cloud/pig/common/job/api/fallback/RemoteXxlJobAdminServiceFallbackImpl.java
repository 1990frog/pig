package com.pig4cloud.pig.common.job.api.fallback;

import com.pig4cloud.pig.common.job.api.RemoteXxlJobAdminService;
import com.pig4cloud.pig.common.job.api.XxlJobInfo;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/8/2x
 */
@Slf4j
@Component
public class RemoteXxlJobAdminServiceFallbackImpl implements RemoteXxlJobAdminService {

	@Setter
	private Throwable cause;

	@Override
	public ReturnT<String> add(XxlJobInfo jobInfo) {
		return null;
	}

	@Override
	public ReturnT<String> update(XxlJobInfo jobInfo) {
		return null;
	}

	@Override
	public ReturnT<String> remove(int id) {
		return null;
	}

	@Override
	public ReturnT<String> pause(Integer id) {
		return null;
	}

	@Override
	public ReturnT<String> start(Integer id) {
		return null;
	}

	@Override
	public ReturnT<String> triggerJob(int id, String executorParam, String addressList) {
		return null;
	}
}
