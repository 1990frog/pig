package com.pig4cloud.pig.admin.api.dto;

import com.pig4cloud.pig.admin.api.entity.SysRoleMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @description  角色权限操作对象
 *
 * @author hexun
 * @date 17:16 2021/5/31
 *
 */
@Data
public class RoleMenuOperate {

	/**
	 * 操作数据集合
	 */
	List<Operate> operates = new ArrayList<>();

	@Data
	public static class Operate{
		/**
		 * 操作类型
		 * 0-删除 1-新增
		 */
		private int type;

		/**
		 * menu ID列表
		 */
		private List<SysRoleMenu> list;
	}

}

