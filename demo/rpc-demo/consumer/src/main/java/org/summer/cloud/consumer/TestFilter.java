package org.summer.cloud.consumer;

import org.springframework.stereotype.Component;
import org.summer.cloud.rpc.Filter;
import org.summer.cloud.rpc.Invoker;
import org.summer.cloud.rpc.Request;
import org.summer.cloud.rpc.Response;
@Component
public class TestFilter implements Filter {
	@Override
	public Response invoke(Invoker invoker, Request request) {
		System.out.println("test filter invoke");
		return invoker.invoke(request);
	}

	@Override
	public boolean isConsumer() {
		return true;
	}

	@Override
	public boolean isProvider() {
		return true;
	}
}
