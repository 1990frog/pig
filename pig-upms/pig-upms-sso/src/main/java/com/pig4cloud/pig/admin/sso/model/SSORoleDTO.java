package com.pig4cloud.pig.admin.sso.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SSORoleDTO
 * @Author Duys
 * @Date 2024/9/23 15:24
 */
@Data
public class SSORoleDTO implements Serializable {
	private List<SSORoleInfo> all;
	private List<SSORoleInfo> current;
	private boolean isAdmin;
}
