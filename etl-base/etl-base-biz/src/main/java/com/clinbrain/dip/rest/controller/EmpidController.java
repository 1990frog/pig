package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.rest.bean.SetWhere;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.EmpidService;
import com.clinbrain.dip.util.ExceptionUtil;
import com.clinbrain.dip.util.Pair;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Liaopan on 2018/1/15.
 */
@RestController
@RequestMapping("/etl/empid")
public class EmpidController {

    @Autowired
    private EmpidService empidService;
    /**
     *
     * @param str :  [{"set":{"pers_basicinfo_empiid":1001462812,
     *            "pers_basicinfo_empino":"c82a169b-0c0a-4927-b449-5ca19bd8a3b0"},
     *            "where":{"pers_basicinfo_persid":08236768,"pers_basicinfo_medorgcode":"0011",
     *            "pers_basicinfo_datasourceflag":"ip"}}]
     * @return
     */
    @PostMapping
    public ResponseData save(@RequestBody String str) {
        if(StringUtils.isEmpty(str)) {
            return new ResponseData.Builder<>().error("接收参数为空!");
        }
        List<SetWhere> list = new ArrayList<>();
        try {
            Gson gson = new Gson();
            List<Map> fromJson = gson.fromJson(str, List.class);
            list = fromJson.stream().map(s -> new SetWhere(gson.toJson(s.get("set")), gson.toJson(s.get("where"))))
                    .collect(Collectors.toList());
        }catch(Exception e) {
            return new ResponseData.Builder<>().error("转换JSON出错!" + ExceptionUtil.getStackTrace(e));
        }
        Pair<Integer, Integer> result = empidService.save(list);
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("updated", result.getFirst());
        map.put("inserted", result.getSecond());
        return new ResponseData.Builder<>(map).success();
    }

}
