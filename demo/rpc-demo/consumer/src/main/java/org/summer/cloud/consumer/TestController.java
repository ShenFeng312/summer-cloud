package org.summer.cloud.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.summer.cloud.api.DemoService;
import org.summer.cloud.rpc.annotation.SummerCloudResource;

@RestController
public class TestController {
	@SummerCloudResource(host = "http://provider:8080")
	private DemoService demoService;
	@GetMapping("/hello")
	public String hello(@RequestParam String name){
		return demoService.hello(name);
	}
}
