package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLTokenCategory;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLTokenCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class TokenCategoryService extends BaseService<ETLTokenCategory> {
    @Autowired
    DBETLTokenCategoryMapper categoryMapper;

    @Autowired
    private DBETLModuleMapper etlModuleMapper;

    public int appendTokenCategory(ETLTokenCategory dipTokenCategory) {
        dipTokenCategory.setCreatedAt(new Date());
        dipTokenCategory.setUpdatedAt(new Date());
        return categoryMapper.insert(dipTokenCategory);
    }

    public int putTokenCategory(ETLTokenCategory dipTokenCategory) {
        return categoryMapper.updateTokenCategory(dipTokenCategory);
    }
}