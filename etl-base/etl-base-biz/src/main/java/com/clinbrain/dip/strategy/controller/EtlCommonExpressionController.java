package com.clinbrain.dip.strategy.controller;

import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.pojo.EtlCommonExpression;
import com.clinbrain.dip.strategy.service.EtlCommonExpressionService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (EtlCommonExpression)表控制层
 *
 * @author Liaopan
 * @since 2021-01-11 17:54:06
 */
@Api(value = "/expr", tags = "")
@Slf4j
@RestController
@RequestMapping("expr")
public class EtlCommonExpressionController extends ApiBaseController {
	/**
	 * 服务对象
	 */
	@Autowired
	private EtlCommonExpressionService etlCommonExpressionService;

	/**
	 * 分页查询所有数据
	 *
	 * @param page                分页对象
	 * @param etlCommonExpression 查询实体
	 * @return 所有数据
	 */
	@GetMapping
	public R selectAll(PageParam page, @MultiRequestBody EtlCommonExpression etlCommonExpression) {
		return success(this.etlCommonExpressionService.selectPageAll(page.getPage(), page.getSize(), etlCommonExpression));
	}

	/**
	 * 查询所有数据，不要分页，不要过滤
	 * @return
	 */
	@GetMapping("/all")
	public R selectAll() {
		return success(this.etlCommonExpressionService.selectAll());
	}

	/**
	 * 通过主键查询单条数据
	 *
	 * @param id 主键
	 * @return 单条数据
	 */
	@GetMapping("{id}")
	public R selectOne(@PathVariable Object id) {
		return R.ok(this.etlCommonExpressionService.selectOne(id));
	}

	/**
	 * 新增数据
	 *
	 * @param etlCommonExpression 实体对象
	 * @return 新增结果
	 */
	@PostMapping
	public R add(@RequestBody EtlCommonExpression etlCommonExpression) {
		return R.ok(this.etlCommonExpressionService.insertNonNull(etlCommonExpression));
	}

	/**
	 * 修改数据
	 *
	 * @param etlCommonExpression 实体对象
	 * @return 修改结果
	 */
	@PutMapping
	public R edit(@RequestBody EtlCommonExpression etlCommonExpression) {
		return R.ok(this.etlCommonExpressionService.updateByPrimaryKey(etlCommonExpression));
	}

	/**
	 * 删除数据
	 *
	 * @param idList 主键结合
	 * @return 删除结果
	 */
	@DeleteMapping
	public R delete(@RequestParam("idList") List<Long> idList) {
		return success(this.etlCommonExpressionService.deleteByIds(idList));
	}
}
