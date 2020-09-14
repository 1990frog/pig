package com.clinbrain.dip.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Liaopan on 2020-08-31.
 */
@Configuration
public class JacksonObjectMapper extends ObjectMapper {
	public JacksonObjectMapper() {
		this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
}
