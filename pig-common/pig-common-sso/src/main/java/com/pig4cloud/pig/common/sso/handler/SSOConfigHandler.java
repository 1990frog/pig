package com.pig4cloud.pig.common.sso.handler;

import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.common.sso.component.SSOClientInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class SSOConfigHandler implements HandlerFunction<ServerResponse> {

	private final SSOClientInfo ssoClientInfo;

	@Override
	public Mono<ServerResponse> handle(ServerRequest serverRequest) {

		// 转换流信息写出
		FastByteArrayOutputStream os = new FastByteArrayOutputStream();
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, String> appNameMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[0]));
		String sysClass = serverRequest.queryParam("sysClass").orElse(serverRequest.headers().firstHeader("sysClass"));
		resultMap.put("ssoEnable", ssoClientInfo.isEnable());
		resultMap.put("serverUrl", ssoClientInfo.isEnable() ? ssoClientInfo.getServerUrl() + appNameMap.get(sysClass) : "");
		try {
			final String str = JSONUtil.toJsonStr(resultMap);
			os.write(str.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromResource(new ByteArrayResource(os.toByteArray())));
	}

}