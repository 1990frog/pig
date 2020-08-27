package com.clinbrain.dip.tactics.util;

import com.clinbrain.dip.pojo.ETLConnection;
import com.pig4cloud.pig.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Liaopan on 2020/8/17 0017.
 */
@Slf4j
public class ServiceUtil<T> {
	private T t;

	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}

	public static <T> T convert(R r) {
		try {
			return objectMapper.readValue(objectMapper.writeValueAsString(r.getData()),
				new TypeReference<T>(){});
		} catch (IOException e) {
			log.error("转换异常", e);
			return null;
		}
	}
}
