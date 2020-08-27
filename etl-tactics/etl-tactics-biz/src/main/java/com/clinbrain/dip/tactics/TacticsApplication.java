package com.clinbrain.dip.tactics;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Liaopan on 2020/8/13 0013.
 */
@EnablePigFeignClients
@EnablePigResourceServer
@SpringCloudApplication
public class TacticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacticsApplication.class, args);
	}
}
