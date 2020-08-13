package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLComponent;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 20180619 ， 添加dataxflow，代码上跟component放一起
 */
@RestController
@RequestMapping("/etl/component")
public class ComponentController {

    @Autowired
    ComponentService dipComponentService;

    /**
     * 获取所有组件
     * @return
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseData getAllComponent(){
        Example example = new Example(ETLComponent.class);
        example.createCriteria().andEqualTo("display",1);
        List<ETLComponent> dipComponents = dipComponentService.selectByExample(example);
        return new ResponseData.Builder<List>(dipComponents).success();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseData DeleteComponent(@PathVariable("id") int id){
        ETLComponent component=new ETLComponent();
        component.setId(id);
        return new ResponseData.Builder<>(dipComponentService.delete(component)).success();
    }

    @PutMapping
    public ResponseData renovateComponent(@RequestBody ETLComponent component){
        if(component==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipComponentService.putComponent(component)).success();
    }

    @PostMapping
    public ResponseData appendComponent(@RequestBody ETLComponent component){
        if(component==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipComponentService.appendComponent(component)).success();
    }

    /**
     * 获取所有DataX类型的组件
     * @return
     */
    @GetMapping("/dataflows")
    public ResponseData alldataxflow(){
        return new ResponseData.Builder<>(dipComponentService.selectAllDataxflow()).success();
    }

}
