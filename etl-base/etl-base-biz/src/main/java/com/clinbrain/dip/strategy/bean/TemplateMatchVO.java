package com.clinbrain.dip.strategy.bean;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.NumberUtil;
import com.clinbrain.dip.strategy.entity.Template;
import lombok.Data;

/**
 * Created by Liaopan on 2020-10-12.
 * 主要用来展示模板的匹配结果
 */
@Data
public class TemplateMatchVO implements Comparable {
	private Template templateInfo;

	private Double matchedRate = 0d;

	private String rateFormat;

	@Override
	public int compareTo(Object o) {
		if(o == null) {
			return -1;
		}
		TemplateMatchVO vo = (TemplateMatchVO) o;
		if(vo.getMatchedRate() == null) {
			return -1;
		}
		if(this.getMatchedRate() == null) {
			return 1;
		}
		return vo.getMatchedRate().compareTo(this.getMatchedRate());

	}

	public String getRateFormat() {
		return NumberUtil.decimalFormat("#.##%",this.matchedRate);
	}
}
