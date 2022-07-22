package com.pig4cloud.pig.admin.api.dto;

import com.pig4cloud.pig.admin.api.entity.SysMenu;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName MenuTreeDTO
 * @Author Duys
 * @Description
 * @Date 2021/7/15 16:38
 **/
@Data
public class MenuTreeDTO {

	private Integer roleId;

	private Boolean hasPermission;

	/**
	 * 菜单ID
	 */
	private Integer menuId;

	/**
	 * 菜单名称
	 */
	private String name;

	/**
	 * 菜单权限标识
	 */
	private String permission;

	/**
	 * 父菜单ID
	 */
	private Integer parentId;

	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 前端路由标识路径
	 */
	private String path;

	/**
	 * 排序值
	 */
	private Integer sort;

	/**
	 * 菜单类型 （0菜单 1按钮）
	 */
	private String type;

	/**
	 * 是否缓冲
	 */
	private String keepAlive;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 0--正常 1--删除
	 */
	private String delFlag;

	public List<MenuTreeDTO> child;

	public MenuTreeDTO() {
	}

	public MenuTreeDTO(SysMenu m) {
		BeanUtils.copyProperties(m, this);
	}


}
