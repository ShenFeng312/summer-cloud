package org.summer.cloud.consumer;

import org.summer.cloud.api.DemoService;
import org.summer.cloud.rpc.annotation.SummerCloudService;

@SummerCloudService
public class DemoServiceImpl implements DemoService {
	@Override
	public String hello(String name) {
		return "hello,"+name;
	}
}
