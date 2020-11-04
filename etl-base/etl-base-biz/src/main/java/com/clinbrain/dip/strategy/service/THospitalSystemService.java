package com.clinbrain.dip.strategy.service;

import com.clinbrain.dip.rest.service.BaseService;
import com.clinbrain.dip.strategy.entity.HospitalSystem;
import com.clinbrain.dip.strategy.mapper.THospitalSystemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
