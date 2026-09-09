package com.pulsesg.platform.core.app.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@RefreshScope
@EnableAsync
@ComponentScan(basePackages = "com.pulsesg.*")
public class PulsesgAppServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(PulsesgAppServiceApplication.class, args);
	}
}
