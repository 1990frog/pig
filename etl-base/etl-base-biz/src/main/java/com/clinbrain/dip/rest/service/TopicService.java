package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLTopic;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class TopicService extends BaseService<ETLTopic> {
    @Autowired
    private DBETLTopicMapper etlTopicMapper;

    @Autowired
    private DBETLModuleMapper etlModuleMapper;

    public boolean putTopic(ETLTopic topic) {
        return etlTopicMapper.updateTopic(topic);
    }


    public boolean appendTopic(ETLTopic etlTopic) {
        ETLTopic avertTopic = new ETLTopic();
        avertTopic.setTopicName(etlTopic.getTopicName());
        if (etlTopicMapper.select(avertTopic).size() > 0) {
            return false;
        } else {
            etlTopic.setCreatedAt(new Date());
            etlTopic.setUpdatedAt(new Date());
            return etlTopicMapper.insert(etlTopic) > 0;
        }
    }

    public int checkTopicIdByJob(Integer topicId) {
        return etlModuleMapper.checkTopicIdByJob(topicId);
    }
}