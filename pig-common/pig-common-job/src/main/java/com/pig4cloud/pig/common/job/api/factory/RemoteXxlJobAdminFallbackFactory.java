package com.pig4cloud.pig.common.job.api.factory;

import com.pig4cloud.pig.common.job.api.RemoteXxlJobAdminService;
import com.pig4cloud.pig.common.job.api.fallback.RemoteXxlJobAdminFallbackImpl;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteXxlJobAdminFallbackFactory implements FallbackFactory<RemoteXxlJobAdminService> {

	@Override
	public RemoteXxlJobAdminService create(Throwable throwable) {
		RemoteXxlJobAdminFallbackImpl remoteXxlJobAdminFallback = new RemoteXxlJobAdminFallbackImpl();
		remoteXxlJobAdminFallback.setCause(throwable);
		return remoteXxlJobAdminFallback;
	}
}
