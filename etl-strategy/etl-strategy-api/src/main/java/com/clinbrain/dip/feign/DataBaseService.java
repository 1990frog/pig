package com.clinbrain.dip.feign;

import com.clinbrain.dip.pojo.ETLConnection;
import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Created by Liaopan on 2020/7/29 0029.
 */
@FeignClient(contextId = "dataBaseService", value = ServiceNameConstants.ETL_SERVICE)
public interface DataBaseService {

	@GetMapping("/clinbrain/api/etl/connection")
	public List<ETLConnection> getAll();


}
