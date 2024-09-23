package com.pig4cloud.pig.admin.sso.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SSORoleInfo
 * @Author Duys
 * @Description 角色信息
 * @Date 2021/12/14 10:41
 **/
@Data
public class SSORoleInfo implements Serializable {
	private String roleCode;
	private String roleName;
}
