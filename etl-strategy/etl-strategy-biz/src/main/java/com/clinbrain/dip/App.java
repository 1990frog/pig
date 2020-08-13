package com.clinbrain.dip;

import com.pig4cloud.pig.common.security.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author pig archetype
 * <p>
 * 项目启动类
 */
@EnablePigFeignClients
@EnablePigResourceServer
@SpringCloudApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
