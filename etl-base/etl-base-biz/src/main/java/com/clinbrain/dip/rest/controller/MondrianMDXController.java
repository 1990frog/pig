package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.metadata.ConnectionPoolProperty;
import com.clinbrain.dip.rest.bean.PropertyBean;
import com.clinbrain.dip.rest.jdbc.MondrianJdbcHelper;
import com.clinbrain.dip.rest.request.SQLRequest;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.response.SQLResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mondrian.olap.CacheControl;
import mondrian.rolap.CacheControlImpl;
import mondrian.rolap.RolapSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/mondrian")
public class MondrianMDXController {
    private static final Logger logger = LoggerFactory.getLogger(MondrianMDXController.class);

    private ConnectionPoolProperty properties;
    private final String rolapPrefix = "实时-";

    @Autowired
    private PropertyBean propertyBean;

    @PostConstruct
    public void init() {
        properties = new ConnectionPoolProperty();
        properties.setDriverName(propertyBean.getDriverName());
        properties.setMaxTotal(propertyBean.getMaxTotal());
        properties.setMaxIdle(propertyBean.getMaxIdle());
        properties.setMinIdle(propertyBean.getMinIdle());
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public SQLResponse query(@RequestBody SQLRequest sqlRequest) {
        SQLResponse response = null;
        String measure_pattern = "(\\[\\s*Measures\\s*\\]\\.)?\\[(?<measure>.*?)\\]";
        String mdx;
        Set<String> measureSets = Sets.newHashSet();
        boolean rolapFlag = false;
        URL url;

        try {
            mdx = sqlRequest.getMdxDescData();
            Pattern pattern = Pattern.compile(measure_pattern);
            Matcher matcher = pattern.matcher(mdx);
            while (matcher.find()) {
                measureSets.add(matcher.group("measure"));
            }
            for (String str : measureSets) {
                if (str.startsWith(rolapPrefix)) {
                    rolapFlag = true;
                    break;
                }
            }

            pattern = Pattern.compile(propertyBean.getDiseasePattern()); // Disease MDX
            matcher = pattern.matcher(mdx);
            if (matcher.find() && measureSets.size() > 0) {
                for (String str : measureSets) {
                    mdx = mdx.replace("[" + str + "]", "[" + str + propertyBean.getDiseaseSuffix() + "]");
                }
            }

            if(rolapFlag) {
                url = Thread.currentThread().getContextClassLoader().getResource(String.format("%s.xml", propertyBean.getRolapSchema()));
                properties.setUrl(String.format(propertyBean.getRolapUrl(), url.getPath()));
                properties.setType(ConnectionPoolProperty.ConnectionType.ROLAP);
            } else {
                url = Thread.currentThread().getContextClassLoader().getResource(String.format("%s.xml", propertyBean.getMolapSchema()));
                properties.setUrl(String.format(propertyBean.getMolapUrl(), url.getPath()));
                properties.setType(ConnectionPoolProperty.ConnectionType.MOLAP);
            }

            MondrianJdbcHelper jdbc = new MondrianJdbcHelper(properties);
            response = jdbc.executeMDXQuery(mdx);
        } catch (Exception e) {
            logger.error("Execute mondrian mdx failed: \n" + sqlRequest.getMdxDescData() + "\nError message: \n" + e);
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/flush/{schemaName}")
    public ResponseData<String> cleanCache(@PathVariable("schemaName") String schemaName) {
        AtomicReference<String> message = new AtomicReference<>(String.format("Not found specific schema: %s", schemaName));
        List<RolapSchema> rolapSchemaList = mondrian.rolap.RolapSchema.getRolapSchemas();
        Optional<RolapSchema> rolapSchema = rolapSchemaList.stream().filter(schema -> schema.getName().contains(schemaName)).findFirst();
        rolapSchema.ifPresent(schema -> {
            schema.getInternalConnection().getCacheControl(null).flushSchemaCache();
            message.set(String.format("Cleaned schema %s cache", schemaName));
        });
        return new ResponseData.Builder<AtomicReference<String>>().data(message).success();
    }

    @RequestMapping(value = "/flush")
    public void cleanAllCache() {
        CacheControl control = new CacheControlImpl(null);
        control.flushSchemaCache();
    }

    @RequestMapping(value = "/query_concurrent", method = RequestMethod.POST)
    public void queryConcurrent(@RequestBody SQLRequest sqlRequest) {
        int running_query_counter = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(running_query_counter);
        List<SQLRequest> list = Lists.newArrayList();
        for (int i = 0; i < running_query_counter * 10; i++) {
            list.add(sqlRequest);
        }
        for (SQLRequest req : list) {
            executorService.submit(new MondrianThread(req));
        }
        executorService.shutdown();
        boolean isFinish = false;
        while (!isFinish) {
            try {
                isFinish = executorService.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public class MondrianThread implements Runnable {
        private SQLRequest sqlRequest;

        public MondrianThread(SQLRequest sqlRequest) {
            this.sqlRequest = sqlRequest;
        }

        @Override
        public void run() {
            query(sqlRequest);
        }
    }
}
