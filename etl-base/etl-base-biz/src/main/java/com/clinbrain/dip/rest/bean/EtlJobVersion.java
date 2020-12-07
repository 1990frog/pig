package com.clinbrain.dip.rest.bean;

import com.clinbrain.dip.pojo.ETLJob;
import com.clinbrain.dip.strategy.entity.Template;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Created by Liaopan on 2020-11-06.
 */
public class EtlJobVersion extends ETLJob {

	@Getter
	@Setter
	private Template template;

	@Getter
	@Setter
	private List<JobHistory> jobVersion;

	@Getter
	@Setter
	private List<String> checkedModules;

	@Getter
	@Setter
	private List<String> notCheckedModules;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class JobHistory {
		private Date createDate;
		private String desc;
	}
}
