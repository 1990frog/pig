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

/**
 * @author liaopan
 * @date 用来判断是否开启sso登录
 *//*
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
		Map<String,String> appNameMap = ssoClientInfo.getApps().stream().collect(Collectors.toMap(s -> s.split("\\|")[2], s -> s.split("\\|")[0]));
		String sysClass = serverRequest.queryParam("sysClass").orElse(serverRequest.headers().firstHeader("sysClass"));
		resultMap.put("ssoEnable", ssoClientInfo.isEnable());
		resultMap.put("serverUrl", ssoClientInfo.isEnable()? ssoClientInfo.getServerUrl() + appNameMap.get(sysClass): "");
		try {
			final String str = JSONUtil.toJsonStr(resultMap);
			os.write(str.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromResource(new ByteArrayResource(os.toByteArray())));
	}

}*/
