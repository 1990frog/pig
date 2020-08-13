package com.clinbrain.dip.tactics;

import com.pig4cloud.pig.common.security.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * Created by Liaopan on 2020/8/13 0013.
 */
@EnablePigFeignClients
@EnablePigResourceServer
@SpringCloudApplication
public class TraticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraticsApplication.class, args);
	}
}
