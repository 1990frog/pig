
package com.pig4cloud.pig.common.job.api.factory;

import com.pig4cloud.pig.common.job.api.RemoteXxlJobAdminService;
import com.pig4cloud.pig.common.job.api.fallback.RemoteXxlJobAdminServiceFallbackImpl;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/8/1
 */
@Component
public class RemoteXxlJobAdminServiceFallbackFactory implements FallbackFactory<RemoteXxlJobAdminService> {

	@Override
	public RemoteXxlJobAdminService create(Throwable throwable) {
		RemoteXxlJobAdminServiceFallbackImpl remoteXxlJobAdminServiceFallback = new RemoteXxlJobAdminServiceFallbackImpl();
		remoteXxlJobAdminServiceFallback.setCause(throwable);
		return remoteXxlJobAdminServiceFallback;
	}

}
