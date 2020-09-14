package com.clinbrain.dip.rest.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import javax.ws.rs.DefaultValue;
import java.util.List;

/**
 * 通用CURD service
 * 其他单表操作使用这个service，传入对应的model class就行
 * Created by Liaopan on 2018/1/15.
 */
@Service
public abstract class BaseService<T> {

    protected final Logger logger = LoggerFactory.getLogger(BaseService.class);

    @Autowired
    protected Mapper<T> mapper ;

    /**
     * 根据类型属性值查询，属性值不为空的为sql中的where条件内容
     * @param t
     * @return
     */
    public List<T> select(T t){
        logger.debug("select:"+t);
        return mapper.select(t);
    }

    public T selectOne(Object id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * 查询所有
     * @return
     */
    public List<T> selectAll(){
        return mapper.selectAll();
    }

    public Page<T> selectAll(int pageNum, int pageSize){
        logger.debug("selectAll,page parameter: pageNum:"+ pageNum+",pageSize:"+pageSize);
        PageHelper.startPage(pageNum,pageSize);
        return (Page<T>) mapper.selectAll();
    }

	public Page<T> selectPageAll(int pageNum, int pageSize, T t){
		logger.debug("selectPageAll, page parameter: pageNum:"+ pageNum+",pageSize:"+pageSize);
		PageHelper.startPage(pageNum, pageSize);
		return (Page<T>) mapper.select(t);
	}

    /**
     * 自定义查询条件，具体可以查看example用法
     * @param example
     * @return
     */
    public List<T> selectByExample(Example example){
        logger.debug("selectByExample:" + example);
        return mapper.selectByExample(example);
    }

    /**
     * 查总数
     * @param t
     * @return
     */
    public int selectCount(T t){
        logger.debug("selectCount:" + t);
        return mapper.selectCount(t);
    }

    /**
     * 自定义查询条件查总数
     * @param example
     * @return
     */
    public int selectCountByExample(Example example){
        logger.debug("selectCountByExample:" + example);
        return mapper.selectCountByExample(example);
    }

    /**
     * 新增
     * @param t
     * @return
     */
    public int insert(T t){
        logger.debug("insert:" + t);
        return mapper.insert(t);
    }

    /**
     * 新增，只插入属性不为null的值
     * @param t
     * @return
     */
    public int insertNonNull(T t){
        logger.debug("insertNotNull:" + t);
        return mapper.insertSelective(t);
    }

    public int delete(T t){
        logger.debug("delete:" + t);
        return mapper.delete(t);
    }

    public int deleteByPrimaryKey(T t){
        logger.debug("deleteByPrimaryKey:" + t);
        return mapper.deleteByPrimaryKey(t);
    }

    public int deleteByExample(Example example){
        logger.debug("deleteByExample:" + example);
        return mapper.deleteByExample(example);
    }

    public int updateByPrimaryKey(T t){
        logger.debug("updateByPrimaryKey:" + t);
        return mapper.updateByPrimaryKey(t);
    }

    public int updateNonNull(T t){
        logger.debug("updateNonNull:" + t);
        return mapper.updateByPrimaryKeySelective(t);
    }

    public int updateByExample(T t,Example example){
        logger.debug("updateByExample,t ->" + t);
        logger.debug("updateByExample, example ->" + example);
        return mapper.updateByExample(t,example);
    }

    public int updateByExampleSelective(T t,Example example){
        logger.debug("updateByExampleSelective,t ->" + t);
        logger.debug("updateByExampleSelective, example ->" + example);
        return mapper.updateByExampleSelective(t,example);
    }
}
