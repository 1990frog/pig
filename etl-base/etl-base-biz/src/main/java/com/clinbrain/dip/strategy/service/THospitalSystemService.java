package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.strategy.entity.HospitalSystem;
import com.clinbrain.dip.strategy.mapper.THospitalSystemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (THospitalSystem)表服务接口
 *
 * @author Liaopan
 * @since 2020-10-30 16:04:26
 */
@Service
@RequiredArgsConstructor
public class THospitalSystemService extends BaseService<HospitalSystem> {

	private final THospitalSystemMapper tHospitalSystemMapper;


	public Map<String, List<HospitalSystem>> getAll() {
		final List<HospitalSystem> list = selectAll();
		return list.stream().collect(Collectors.groupingBy(HospitalSystem::getVendor));
	}
}
