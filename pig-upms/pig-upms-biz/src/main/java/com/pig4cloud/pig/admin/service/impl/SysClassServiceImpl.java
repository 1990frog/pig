package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.api.entity.SysSystem;
import com.pig4cloud.pig.admin.mapper.SysClassMapper;
import com.pig4cloud.pig.admin.service.SysClassService;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysClassServiceImpl  extends ServiceImpl<SysClassMapper, SysSystem> implements SysClassService {

	@Override
	public SysSystem getSysClassById(Integer sysId) {
		return baseMapper.selectById(sysId);
	}

	@Override
	public IPage getPageSysClass(Page page, SysSystem sysSystem) {
		return baseMapper.selectPage(page, sysSystem);
	}

	@Override
	public Boolean deleteSysClass(Integer sysId) {
		return baseMapper.deleteById(sysId) == 1;
	}

	@Override
	public Boolean editSysClass(SysSystem sysSystem) {
		SysSystem old = baseMapper.selectById(sysSystem.getSysId());
		if(old == null){
			log.error("标识为[{}]的系统不存在，修改失败", sysSystem.getSysClass());
			return false;
		}
		if(StrUtil.isNotBlank(sysSystem.getSysClass()) && !old.getSysClass().equals(sysSystem.getSysClass())){
			log.error("不允许修改系统标识");
			return false;
		}
		/*if(StrUtil.isNotBlank(sysSystem.getSysName()) && !old.getSysName().equals(sysSystem.getSysName())){
			log.error("名称为[{}]的系统已存在，修改失败", sysSystem.getSysClass());
			return false;
		}*/
		sysSystem.setOperateUser(SecurityUtils.getUser().getUsername());
		return baseMapper.updateById(sysSystem) == 1;
	}

	@Override
	public Boolean addSysClass(SysSystem sysSystem) {
		SysSystem condition = new SysSystem();
		condition.setSysClass(sysSystem.getSysClass());
		condition.setSysName(sysSystem.getSysName());
		Wrapper queryWrapper = new QueryWrapper<>(condition);
		int count = baseMapper.selectCount(queryWrapper);
		if(count > 0){
			log.error("新增系统失败，已存在系统名称[{}]或标识[{}]", sysSystem.getSysName(), sysSystem.getSysClass());
			return false;
		}
		sysSystem.setStatus("1"); //新增默认为启用状态
		sysSystem.setOperateUser(SecurityUtils.getUser().getUsername());
		return baseMapper.insert(sysSystem) == 1;
	}
}
