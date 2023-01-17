package org.summer.cloud.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.summer.cloud.api.DemoService;
import org.summer.cloud.rpc.annotation.SummerCloudResource;

@SpringBootApplication
public class Application {

	@SummerCloudResource(host = "localhost:9090")
	DemoService demoService;
	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
		DemoService bean = run.getBean(DemoService.class);
		System.out.println(bean.hello("test"));
	}
}
