package com.clinbrain.dip.feign;

import com.clinbrain.dip.pojo.ETLConnection;
import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Created by Liaopan on 2020/7/29 0029.
 */
@FeignClient(contextId = "dataBaseService", value = ETLBaseServiceNameConstant.ETL_SERVICE)
public interface DataBaseService {

	@GetMapping("/clinbrain/api/etl/connection/all")
	public R getAll();

}
