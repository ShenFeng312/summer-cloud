package org.summer.cloud.rpc.http;

import org.springframework.web.reactive.function.client.WebClient;
import org.summer.cloud.rpc.Invoker;
import org.summer.cloud.rpc.Request;
import org.summer.cloud.rpc.Response;
import org.summer.cloud.rpc.ResponseImpl;

/**
 * @author shenfeng
 */
public class HttpInvoker implements Invoker {
	private static  WebClient webClient = WebClient.builder().build();


	@Override
	public Response invoke(Request request) {
		ResponseImpl response = new ResponseImpl();
		try {

			Object result = webClient.post().uri(request.getHost()).bodyValue(request.getArgs()).header("service",
							request.getService().getName()).header("method", request.getMethod().getName()).retrieve()
					.bodyToMono(request.getMethod().getReturnType())
					.block();
			response.setResult(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

}
