package com.clinbrain.dip.strategy.controller;

import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.strategy.service.SqlQueryService;
import com.pig4cloud.pig.common.core.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2021-01-13.
 */
@RestController
@RequestMapping("/sql")
@RequiredArgsConstructor
public class SqlExecutorController extends ApiBaseController{

	private final SqlQueryService sqlQueryService;
	/**
	 * 执行sql.返回近10条记录
	 * @param connectionCode 连接字符串
	 * @param sql 执行的sql
	 * @return
	 */
	@GetMapping("query")
	public R sqlQuery(String connectionCode, String sql) throws Exception {
		return success(sqlQueryService.queryList(connectionCode, sql));
	}

	@PostMapping("query")
	public R sqlQueryPost(@MultiRequestBody String connectionCode, @MultiRequestBody String sql) {
		try {
			return success(sqlQueryService.queryList(connectionCode, sql));
		}catch (Exception e) {
			return failed(e.getMessage());
		}
	}
}
