package com.clinbrain.dip.rest.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class PropertyBean {
    @Value(value = "${kylin.url}")
    private String kylinHostname;
    @Value(value = "${kylin.username}")
    private String kylinUsername = "ADMIN";
    @Value(value = "${kylin.password}")
    private String kylinPassword = "KYLIN";

    @Value(value = "${kylin.cube.storage-year}")
    private int kylinStorageYear = 5;

    @Value(value = "${kylin.cube.range-month}")
    private int kylinRangeMonth = 6;

    @Value(value = "${mondrian.mdx.disease.pattern}")
    private String diseasePattern;
    @Value(value = "${mondrian.mdx.disease.suffix}")
    private String diseaseSuffix;
    @Value(value = "${mondrian.jdbc.kylin.url}")
    private String molapUrl;
    @Value(value = "${mondrian.jdbc.impala.url}")
    private String rolapUrl;
    @Value(value = "${mondrian.jdbc.driver.name}")
    private String driverName;
    @Value(value = "${mondrian.jdbc.pool-max-total}")
    private Integer maxTotal;
    @Value(value = "${mondrian.jdbc.pool-max-idle}")
    private Integer maxIdle;
    @Value(value = "${mondrian.jdbc.pool-min-idle}")
    private Integer minIdle;
    @Value(value = "${mondrian.jdbc.molap-cube-schema}")
    private String molapSchema;
    @Value(value = "${mondrian.jdbc.rolap-cube-schema}")
    private String rolapSchema;
}
