package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLEngine;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.EngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/etl/engine")
public class EngineController {

    @Autowired
    EngineService dipEngineService;

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseData getAllEngines(){
        List<ETLEngine> etlEngines = dipEngineService.selectAll();
        return new ResponseData.Builder<List>(etlEngines).success();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseData DeleteTopic(@PathVariable("id") int id){
        ETLEngine topic=new ETLEngine();
        topic.setId(id);
        return new ResponseData.Builder<>(dipEngineService.delete(topic)).success();
    }

    @PutMapping
    public ResponseData renovateTopic(@RequestBody ETLEngine etlTopic){
        if(etlTopic==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipEngineService.putEngine(etlTopic)).success();
    }

    @PostMapping
    public ResponseData appendTopic(@RequestBody ETLEngine etlTopic){
        if(etlTopic==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipEngineService.appendEngine(etlTopic)).success();
    }

}
