package com.pig4cloud.pig.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 *
 * @description  系统类型模型
 *
 * @author hexun
 * @date 17:40 2021/7/14
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysSystem extends Model<SysSystem> {

	private static final long serialVersionUID = -1L;

	//@TableId(value = "sys_id", type = IdType.AUTO)
	@ApiModelProperty(value = "主键id")
	private Integer sysId;

	@NotBlank(message = "系统名称 不能为空")
	@ApiModelProperty(value = "系统名称")
	private String sysName;

	/**
	 * 系统标识与系统名称组成唯一索引
	 */
	@NotBlank(message = "系统标识 不能为空")
	@ApiModelProperty(value = "系统标识")
	private String sysClass;

	@ApiModelProperty(value = "访问地址")
	private String url;

	@ApiModelProperty(value = "状态 0-停用，1-启用")
	private String status;

	@ApiModelProperty(value = "创建时间")
	private Date createTime;

	@ApiModelProperty(value = "更新时间")
	private Date updateTime;

	@ApiModelProperty(value = "操作人")
	private String operateUser;

}
