package com.clinbrain.dip.tactics.controller;

import com.pig4cloud.pig.common.core.util.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/13 0013.
 */
@RestController
@RequestMapping("/tactics")
public class IndexController {

	public R index() {
		return R.ok("hello, tactics");
	}
}
