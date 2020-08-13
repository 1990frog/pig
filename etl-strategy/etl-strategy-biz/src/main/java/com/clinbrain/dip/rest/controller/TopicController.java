package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLTopic;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Liaopan on 2018/1/15.
 */
@RestController
@RequestMapping("/etl/topic")
public class TopicController {

    @Autowired
    public TopicService dipTopicService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseData getAllEngines(@RequestParam(value = "offset", required = false) Integer offset,
                                      @RequestParam(value = "limit", required = false) Integer limit) {
        if (offset != null || limit != null) {
            List<ETLTopic> etlTopics = dipTopicService.selectAll(offset, limit);
            ResponseData.Page pages = new ResponseData.Page(dipTopicService.selectAll().size(), etlTopics);
            return new ResponseData.Builder<ResponseData.Page>(pages).success();
        } else {
            List<ETLTopic> etlTopics = dipTopicService.selectAll();
            return new ResponseData.Builder<List>(etlTopics).success();
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseData deleteTopic(@PathVariable("id") int id) {
        int count = dipTopicService.checkTopicIdByJob(id);
        if (count > 0) {
            return new ResponseData.Builder<>(null).error(String.format("业务已经被 %s 个JOB引用,无法删除",count));
        }
        ETLTopic topic = new ETLTopic();
        topic.setId(id);
        return new ResponseData.Builder<>(dipTopicService.delete(topic)).success();
    }

    @PutMapping
    public ResponseData renovateTopic(@RequestBody ETLTopic etlTopic) {
        if (etlTopic == null) {
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        try {
            return new ResponseData.Builder<>(dipTopicService.putTopic(etlTopic)).success();
        } catch (Exception e) {
            return new ResponseData.Builder<>(null).error(e.getMessage());
        }
    }

    @PostMapping
    public ResponseData appendTopic(@RequestBody ETLTopic etlTopic) {
        if (etlTopic == null) {
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipTopicService.appendTopic(etlTopic)).success();
    }

}
