package org.summer.cloud.rpc.http;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summer.cloud.rpc.FilterManager;
import org.summer.cloud.rpc.Invoker;
import org.summer.cloud.rpc.RequestImpl;
import org.summer.cloud.rpc.Response;
import org.summer.cloud.rpc.annotation.SummerCloudService;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnExpression("false")
@RestController
@RequestMapping("/summer-cloud")
public class HttpRequestController implements BeanPostProcessor {
	@Resource
	private FilterManager filterManager;
	private final Map<String, Invoker> invokerMap = new HashMap<>();
	private final Map<String, Class> classMap = new HashMap<>();
	private final Map<Class, Map<String, Method>> methodMaps = new HashMap<>();

	@PostMapping("/invoke")
	public void invoke(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {

		String serviceName = httpServletRequest.getHeader("service");
		String methodName = httpServletRequest.getHeader("method");
		ServletInputStream inputStream = httpServletRequest.getInputStream();
		Class<?> service = classMap.get(serviceName);
		Method method = methodMaps.get(service).get(methodName);
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] params = null;
		if (parameterTypes.length > 0) {
			try (JSONReader reader = JSONReader.of(inputStream, StandardCharsets.UTF_8)) {
				params = reader.readArray(parameterTypes);
			} catch (Throwable throwable) {
				processError(httpServletResponse, throwable);
				return;
			}
		}
		RequestImpl request = new RequestImpl();
		request.setArgs(params);
		request.setMethod(method);
		request.setService(service);
		Invoker invoker = invokerMap.get(serviceName);

		Response response;
		try {
			response = invoker.invoke(request);
		} catch (Throwable e) {
			processError(httpServletResponse, e);
			return;
		}

		if (response.getThrowable() != null) {
			processError(httpServletResponse, response.getThrowable());
			return;
		}
		if (response.getResult() != null) {
			try (JSONWriter jsonWriter = JSONWriter.ofUTF8()) {
				jsonWriter.writeAny(response.getResult());
				jsonWriter.flushTo(httpServletResponse.getOutputStream(), StandardCharsets.UTF_8);
			}
		}
	}

	private void processError(HttpServletResponse httpServletResponse, Throwable e) throws IOException {
		try (ServletOutputStream outputStream = httpServletResponse.getOutputStream()) {
			httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			outputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
		}
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		SummerCloudService annotation = AnnotationUtils.getAnnotation(bean.getClass(), SummerCloudService.class);
		if (annotation != null) {
			Class<?> beanClass = bean.getClass();
			Class<?> classInterface = beanClass.getInterfaces()[0];
			Invoker provider = HttpProxyConsumerFactory.getProvider(bean, filterManager);

			String className = classInterface.getName();
			invokerMap.put(className, provider);
			classMap.put(className, classInterface);
			Method[] methods = classInterface.getMethods();
			Map<String, Method> methodMap = methodMaps.computeIfAbsent(classInterface, k -> new HashMap<>());
			for (Method method : methods) {
				methodMap.put(method.getName(), method);
			}
		}
		return bean;
	}

}
