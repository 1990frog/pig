package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.feign.factory.RemoteMenuServiceFallbackFactory;
import com.pig4cloud.pig.admin.api.vo.MenuVO;
import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author caijingquan@clinbrain.com
 * @since 2022/11/16
 */
@FeignClient(contextId = "remoteMenuService", value = ServiceNameConstants.UMPS_SERVICE,
		fallbackFactory = RemoteMenuServiceFallbackFactory.class)
public interface RemoteMenuService {

	/**
	 * 通过系统查询菜单
	 *
	 * @param system 系统
	 * @return 菜单
	 */
	@GetMapping("/menu/system")
	List<MenuVO> findMenuBySystem(@RequestParam("system") String system);

}
