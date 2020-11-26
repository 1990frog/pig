package com.clinbrain.dip.strategy.bean;

import com.clinbrain.dip.pojo.ETLModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Liaopan on 2020-11-23.
 * 展示 任务的依赖关系图
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDependencyVO {

	private ETLModule module;

	private List<ModuleDependencyVO> subModules;
}
