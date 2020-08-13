package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.rest.request.SQLRequest;
import com.clinbrain.dip.rest.response.SQLResponse;
import com.clinbrain.dip.rest.service.ImpalaService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/impala")
public class ImpalaController {
    @Autowired
    private ImpalaService impalaService;

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public SQLResponse query(@RequestBody SQLRequest sqlRequest) {
        return impalaService.query(sqlRequest);
    }

    @RequestMapping(value = "/query_concurrent", method = RequestMethod.POST)
    public void queryConcurrent(@RequestBody SQLRequest sqlRequest) {
        int running_query_counter = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(running_query_counter);
        List<SQLRequest> list = Lists.newArrayList();
        for (int i = 0; i < running_query_counter * 2; i++) {
            list.add(sqlRequest);
        }
        for (SQLRequest request : list) {
            executorService.submit(new Thread(() -> query(request)));
        }
        executorService.shutdown();
        boolean isFinish = false;
        while (!isFinish) {
            try {
                isFinish = executorService.awaitTermination(30, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
}
