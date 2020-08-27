package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLTopic;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository
public interface DBETLTopicMapper extends Mapper<ETLTopic> {
    @Update("UPDATE etl_topic SET topic_name = #{topicName},topic_priority = #{topicPriority},updated_at = NOW() WHERE id = #{id}")
    boolean updateTopic(ETLTopic etlTopic);

}
