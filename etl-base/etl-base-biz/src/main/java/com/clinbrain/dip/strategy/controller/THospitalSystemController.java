package com.clinbrain.dip.strategy.controller;

import com.clinbrain.dip.multirequestbody.MultiRequestBody;
import com.clinbrain.dip.strategy.entity.HospitalSystem;
import com.clinbrain.dip.strategy.service.THospitalSystemService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

/**
 * (THospitalSystem)医院，厂商，系统管理
 *
 * @author Liaopan
 * @since 2020-10-31 11:12:54
 */
@Api(value = "/THospitalSystem", tags = "")
@Slf4j
@RestController
@RequestMapping("hospitalSystem")
public class THospitalSystemController extends ApiBaseController {
    /**
     * 服务对象
     */
    @Autowired
    private THospitalSystemService tHospitalSystemService;

    /**
     * 查询所有数据
     *
     * @return 所有数据
     */
    @GetMapping("all")
    public R selectAll() {
		return success(this.tHospitalSystemService.getAll());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Object id) {
        return R.ok(this.tHospitalSystemService.selectOne(id));
    }

    /**
     * 新增数据
     *
     * @param tHospitalSystem 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R add(@RequestBody HospitalSystem tHospitalSystem) {
        return R.ok(this.tHospitalSystemService.insert(tHospitalSystem));
    }

    /**
     * 修改数据
     *
     * @param tHospitalSystem 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R edit(@RequestBody HospitalSystem tHospitalSystem) {
        return R.ok(this.tHospitalSystemService.updateByPrimaryKey(tHospitalSystem));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("id") Long id) {
    	HospitalSystem system = new HospitalSystem();
    	system.setId(id);
        return success(this.tHospitalSystemService.deleteByPrimaryKey(system));
    }
}
