package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.ETLModule;
import com.clinbrain.dip.pojo.ETLTopic;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository
public interface DBETLTopicMapper extends Mapper<ETLTopic> {
    @Update("UPDATE etl_topic SET topic_name = #{topicName},topic_priority = #{topicPriority},updated_at = NOW() WHERE id = #{id}")
    boolean updateTopic(ETLTopic etlTopic);

	/**
	 * 查总数
	 * @param topicId
	 * @return
	 */
	Long totalByTopic(@Param("topicId") Integer topicId);

	List<ETLModule> totalByTopicDetail(@Param("topicId") Integer topicId);

	Long totalOfFailure(@Param("topicId") Integer topicId, @Param("day") int day);

	List<ETLModule> totalOfFailureDetail(@Param("topicId") Integer topicId, @Param("day") int day);

	Long totalOfOverTime(@Param("topicId") Integer topicId, @Param("minute") int minute);

	List<ETLModule> totalOfOverTimeDetail(@Param("topicId") Integer topicId, @Param("minute") int minute);

	Long totalOfRunning(@Param("topicId") Integer topicId);

	List<ETLModule> totalOfRunningDetail(@Param("topicId") Integer topicId);

	Long totalOfRangeModule(@Param("topicId") Integer topicId);

	List<ETLModule> totalOfRangeModuleDetail(@Param("topicId") Integer topicId);
}
