package com.clinbrain.dip.pojo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * null
 * @TableName etl_module_template
 */
@Table(name = "etl_module_template")
@ApiModel(value = "etlModuleTemplate", description = "新增，任务模板")
@Data
public class EtlModuleTemplate {
    /**
     * 
     */
    @Id
    private Integer id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String jsonTemplate;

}