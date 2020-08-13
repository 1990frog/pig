package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLJobScheduler;
import com.clinbrain.dip.pojo.ETLScheduler;
import com.clinbrain.dip.rest.mapper.DBETLSchedulerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Liaopan on 2018/8/1.
 */
@Service
public class EtlSchedulerService extends BaseService<ETLScheduler> {

    @Autowired
    private DBETLSchedulerMapper schedulerMapper;

    public int saveJobScheduler(ETLJobScheduler scheduler){
        return schedulerMapper.saveJobScheduler(scheduler);
    }
}
