package com.pig4cloud.pig.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.entity.SysSystem;
import com.pig4cloud.pig.admin.common.enums.ResponseCodeEnum;
import com.pig4cloud.pig.admin.common.execption.SSOBusinessException;
import com.pig4cloud.pig.admin.service.SysClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysClassServiceImpl implements SysClassService {

	@Override
	public SysSystem getSysClassById(Integer sysId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public IPage getPageSysClass(Page page, SysSystem sysSystem) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean deleteSysClass(Integer sysId) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean editSysClass(SysSystem sysSystem) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}

	@Override
	public Boolean addSysClass(SysSystem sysSystem) {
		throw new SSOBusinessException(ResponseCodeEnum.NOT_SUPPORT);
	}
}
