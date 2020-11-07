package com.clinbrain.dip.strategy.mapper;

import com.clinbrain.dip.strategy.entity.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * (TTemplet)表数据库访问层
 *
 * @author Liaopan
 * @since 2020-09-04 10:45:10
 */
@Mapper
@Repository("templateMapper")
public interface TemplateMapper extends tk.mybatis.mapper.common.Mapper<Template> {

	@Select("select * from t_template where custom != 1")
	List<Template> selectAllPublic();
}
