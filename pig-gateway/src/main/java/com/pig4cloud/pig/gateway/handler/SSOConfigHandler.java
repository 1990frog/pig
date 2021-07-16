/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.gateway.handler;

import cn.hutool.json.JSONUtil;
import com.pig4cloud.captcha.ArithmeticCaptcha;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.gateway.sso.SSOClientInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liaopan
 * @date 用来判断是否开启sso登录
 */
@Slf4j
@Component
@AllArgsConstructor
public class SSOConfigHandler implements HandlerFunction<ServerResponse> {

	private final SSOClientInfo ssoClientInfo;

	@Override
	public Mono<ServerResponse> handle(ServerRequest serverRequest) {

		// 转换流信息写出
		FastByteArrayOutputStream os = new FastByteArrayOutputStream();
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("ssoEnable", ssoClientInfo.isEnable());
		resultMap.put("serverUrl", ssoClientInfo.isEnable()? ssoClientInfo.getServerUrl(): "");
		try {
			final String str = JSONUtil.toJsonStr(resultMap);
			os.write(str.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromResource(new ByteArrayResource(os.toByteArray())));
	}

}
