package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLTopic;
import com.clinbrain.dip.rest.mapper.DBETLModuleMapper;
import com.clinbrain.dip.rest.mapper.DBETLTopicMapper;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.vo.TopicIndexBean;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Liaopan on 2017/10/12.
 */
@Service
public class TopicService extends BaseService<ETLTopic> {
    @Autowired
    private DBETLTopicMapper etlTopicMapper;

    @Autowired
    private DBETLModuleMapper etlModuleMapper;

    private int failureOfDay = 7;

    private int minuteOfOvertime = 30;

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

    public TopicIndexBean indexTotal(Integer topicId,int failureOfDayTemp, int minuteOfOvertimeTemp) {
		return TopicIndexBean.builder().total(etlTopicMapper.totalByTopic(topicId))
				.totalOfFailure(etlTopicMapper.totalOfFailure(topicId, Optional.of(failureOfDayTemp).orElse(failureOfDay)))
				.totalOfOvertime(etlTopicMapper.totalOfOverTime(topicId, Optional.of(minuteOfOvertimeTemp).orElse(minuteOfOvertime)))
				.totalOfRange(etlTopicMapper.totalOfRangeModule(topicId))
				.totalOfRunning(etlTopicMapper.totalOfRunning(topicId)).build();

	}

	public ResponseData.Page<ETLModule> indexTotalDetail(Integer topicId, String type,
														 int failureOfDay, int minuteOfOvertime, int pageNum, int pageSize) {
		Page<ETLModule> pageData;
    	PageHelper.startPage(pageNum, pageSize);
		switch (type) {
			case "totalOfFailure":
				pageData = (Page<ETLModule>) etlTopicMapper.totalOfFailureDetail(topicId, failureOfDay);
				break;
			case "totalOfOvertime":
				pageData = (Page<ETLModule>) etlTopicMapper.totalOfOverTimeDetail(topicId, minuteOfOvertime);
				break;
			case "totalOfRunning":
				pageData = (Page<ETLModule>) etlTopicMapper.totalOfRunningDetail(topicId);
				break;
			case "totalOfRange":
				pageData = (Page<ETLModule>) etlTopicMapper.totalOfRangeModuleDetail(topicId);
				break;
			default:
				pageData =  (Page<ETLModule>) etlTopicMapper.totalByTopicDetail(topicId);
		}
		return new ResponseData.Page<>(pageData.getTotal(),pageData.getResult());

	}
}