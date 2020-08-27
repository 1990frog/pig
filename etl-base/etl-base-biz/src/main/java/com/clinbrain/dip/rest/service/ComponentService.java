package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLComponent;
import com.clinbrain.dip.pojo.ETLDataxflow;
import com.clinbrain.dip.rest.mapper.DBETLComponentMapper;
import com.clinbrain.dip.rest.mapper.DBETLDataxflowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ComponentService extends BaseService<ETLComponent> {
    @Autowired
    @Qualifier("componentMapper")
    private DBETLComponentMapper componentMapper;

    @Autowired
    @Qualifier("dataxflowMapper")
    private DBETLDataxflowMapper dataxflowMapper;

    public boolean putComponent(ETLComponent component){
        return componentMapper.updateComponent(component);
    }

    public boolean appendComponent(ETLComponent component){
        component.setCreatedAt(new Date());
        component.setUpdatedAt(new Date());
        return componentMapper.insert(component)>0;
    }

    public List<ETLDataxflow> selectAllDataxflow(){
        return dataxflowMapper.selectAll();
    }
}
