package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLToken;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class TokenService extends BaseService<ETLToken> {
    @Autowired
    private DBETLTokenMapper tokenMapper;

    @Autowired
    private DBETLModuleMapper etlModuleMapper;

    public boolean appendToken(ETLToken token) {
        token.setCreatedAt(new Date());
        token.setUpdatedAt(new Date());
        return tokenMapper.insert(token) > 0;
    }

    public boolean putToken(ETLToken token) {
        return tokenMapper.updateToken(token);
    }

    public int checkTokenByWorkflowToken(String code) {
        return etlModuleMapper.checkTokenByWorkflowToken(code);
    }
}