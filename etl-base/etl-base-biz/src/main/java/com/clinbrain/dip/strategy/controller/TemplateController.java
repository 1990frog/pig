package com.clinbrain.dip.strategy.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerResponse;
import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.strategy.bean.PackageInfo;
import com.clinbrain.dip.strategy.bean.SystemConnectionCodeVO;
import com.clinbrain.dip.strategy.entity.Template;
import com.clinbrain.dip.strategy.service.TemplateService;
import com.google.common.collect.Lists;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * (TTemplet)表控制层
 *
 * @author Liaopan
 * @since 2020-09-04 09:54:27
 */
@Api(value = "/template", tags = "模板管理")
@RestController
@RequestMapping("template")
@Slf4j
public class TemplateController extends ApiBaseController {
	/**
	 * 服务对象
	 */
	@Resource
	private TemplateService templateService;

	/**
	 * 分页查询所有数据
	 *
	 * @param page      分页对象
	 * @param tTemplate 查询实体
	 * @return 所有数据
	 */
	@ApiOperation(value = "分页展示模板数据", notes = "分页参数{page:{page,size}, Template模板对象}")
	@PostMapping("list")
	public R selectAll(@MultiRequestBody PageParam page, @MultiRequestBody(required = false) Template tTemplate) {
		tTemplate = Optional.ofNullable(tTemplate).orElse(new Template());
		return success(this.templateService.selectPageAll(page.getPage(), page.getSize(), tTemplate));
	}

	@ApiOperation(value = "分组展示模板数据", notes = "选择模板 -> 系统选择")
	@GetMapping("listAll")
	public R selectAllTemplate() {
		final Map<String, List<Template>> map = new HashMap<>();
		final Map<String, Map<String, List<Template>>> resultMap = new HashMap<>();

		Optional.ofNullable(this.templateService.selectAll()).ifPresent(s -> {
			map.putAll(s.stream().collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getSystem()).orElse("OTHER"))));
		});

		if (!map.isEmpty()) {
			map.forEach((key, value) -> {
				resultMap.put(key, value.stream().collect(Collectors.groupingBy(e -> e.getVendor() + e.getSystem() + e.getEdition())));
			});
		}
		return success(resultMap);
	}

	@ApiOperation(value = "生成自定义模板")
	@PostMapping("/generate")
	public R generate(@MultiRequestBody("packageInfo") PackageInfo packageInfo,
					  @MultiRequestBody("jobId") Integer jobId,
					  @MultiRequestBody("moduleCodes") List<String> moduleCodes) {
		try {
			if (templateService.generateTempletFile(packageInfo, jobId, moduleCodes)) {
				return success();
			}
		} catch (Exception e) {
			log.error("生成模板文件出错", e);
			return failed(e.getMessage(), "模板生成出错");
		}
		return failed("未知错误");
	}

	/**
	 * 模板文件上传， 可多选
	 *
	 * @param files 待上传文件
	 * @return 上传状态
	 */
	@ApiOperation(value = "上传模板文件，clb后缀，包含system描述文件")
	@PostMapping("/upload")
	public R upload(@RequestParam("file") MultipartFile[] files) {
		if (files == null || files.length == 0) {
			return failed("没有文件");
		}
		List<UploadResult> results = Lists.newArrayList();
		Stream.of(files).forEach(f -> {
			try {
				templateService.uploadSave(f);
				results.add(new UploadResult(f.getOriginalFilename(), true, "上传成功"));
			} catch (Exception e) {
				log.error("文件上传出错", e);
				results.add(new UploadResult(f.getOriginalFilename(), false, e.getMessage()));
			}
		});
		if (results.stream().anyMatch(r -> !r.isStatus())) {
			return failed(results, "文件上传出错");
		}
		return success(results);
	}

	@ApiOperation("模板下载")
	@GetMapping("export")
	public ResponseEntity<Object> exportTemplate(@RequestParam("id") String id) {
		final Template template = templateService.templateFilePath(id);
		if(StringUtils.isEmpty(template.getTmplPath())) {
			return ResponseEntity.notFound().build();
		}
		File file = new File(template.getTmplPath());

		String fileName = template.getTmplName();
		try{
			fileName = URLEncoder.encode(template.getTmplName(), "UTF-8");
		}catch (Exception e) {
			log.error("文件名称"+template.getTmplName()+"转换异常", e);
		}
		InputStreamResource resource = null;
		try {
			resource = new InputStreamResource( new FileInputStream( file ) );
		} catch (FileNotFoundException e) {
			log.error("文件名称"+template.getTmplName()+"转换异常", e);
			return ResponseEntity.notFound().build();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add ( "Content-Disposition",String.format("attachment;filename=\"%s",fileName));
		headers.add ( "Cache-Control","no-cache,no-store,must-revalidate" );
		headers.add ( "Pragma","no-cache" );
		headers.add ( "Expires","0" );

		ResponseEntity<Object> responseEntity = ResponseEntity.ok()
			.headers ( headers )
			.contentLength ( file.length ())
			.contentType(MediaType.parseMediaType("application/octet-stream" ))
			.body(resource);

		return responseEntity;
	}

	@ApiOperation("模板匹配")
	@PostMapping("matching")
	public R matchingTemplate(@RequestBody List<SystemConnectionCodeVO> templateConnList) {

		return R.ok(templateService.matching(templateConnList));
	}

	@ApiOperation("模板选择后导入")
	@PostMapping("importSave")
	public R importByTemplate(@MultiRequestBody("topicId") Integer topicId,
							  @MultiRequestBody("hospitalCode") String hospitalCode,
							  @MultiRequestBody("templateCode") String templateCode) {
		try {
			final boolean saveModule = templateService.importSaveModule(topicId, hospitalCode, templateCode);
			return saveModule ? R.ok() : R.failed("导入模板任务出错");
		} catch (Exception e) {
			log.error("导入出错", e);
			return R.failed(e.getMessage());
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UploadResult {
		String fileName;
		boolean status;
		String message;
	}
}
