package com.clinbrain.dip.strategy.controller;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liaopan on 2020-09-04.
 */
public class ApiBaseController {

	private final String SUCCESS_MESSAGE = "执行成功";

	protected <T> R success() {
		return R.ok(null, SUCCESS_MESSAGE);
	}

	protected <T> R success(T data) {
		if(data instanceof Page) {
			Page<T> pageData = (Page<T>) data;
			return R.ok(new PageResult<T>(pageData.getPageNum(),pageData.getPageSize(), pageData.getTotal(),pageData.getResult()),
				SUCCESS_MESSAGE);
		}
		return R.ok(data, SUCCESS_MESSAGE);
	}

	protected <T> R success(T data, String message) {

		if(data instanceof Page) {
			Page<T> pageData = (Page<T>) data;
			return R.ok(new PageResult<T>(pageData.getPageNum(),pageData.getPageSize(), pageData.getTotal(),pageData.getResult()),
				StrUtil.emptyToDefault(message, SUCCESS_MESSAGE));
		}
		return R.ok(data, StrUtil.emptyToDefault(message, SUCCESS_MESSAGE));
	}

	protected <T> R<T> failed(T data, String msg) {
		return R.failed(data, msg);
	}

	protected <T> R<T> failed(String msg) {
		return R.failed(msg);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	protected static class PageParam {
		private int page;
		private int size;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class PageResult<T> {
		private int pageNum;
		private int pageSize;
		private long total;
		private List<T> data;
	}

}
