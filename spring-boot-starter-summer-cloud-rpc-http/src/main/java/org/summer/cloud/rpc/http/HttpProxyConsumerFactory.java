package org.summer.cloud.rpc.http;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.summer.cloud.rpc.Filter;
import org.summer.cloud.rpc.FilterChainInvoker;
import org.summer.cloud.rpc.FilterManager;
import org.summer.cloud.rpc.Invoker;
import org.summer.cloud.rpc.Request;
import org.summer.cloud.rpc.RequestImpl;
import org.summer.cloud.rpc.Response;
import org.summer.cloud.rpc.ResponseImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author shenfeng
 */
public class HttpProxyConsumerFactory<T> implements FactoryBean<T>, ApplicationContextAware, BeanNameAware {
	private final FilterManager filterManager;
	private Class<T> clazz;
	private String name;
	private ApplicationContext app;

	public HttpProxyConsumerFactory(FilterManager filterManager) {
		this.filterManager = filterManager;
	}

	public T getConsumer() {
		ConfigurableListableBeanFactory autowireCapableBeanFactory = (ConfigurableListableBeanFactory) app.getAutowireCapableBeanFactory();
		BeanDefinition beanDefinition = autowireCapableBeanFactory.getBeanDefinition(name);
		this.clazz = (Class<T>) beanDefinition.getAttribute("interface");

		List<Filter> consumerFilter = filterManager.getConsumerFilter();
		Invoker invoker = new HttpInvoker();
		Invoker filterChainInvoker = buildFilterChain(invoker, consumerFilter);

		InvocationHandler invocationHandler = (proxy, method, args) -> {

			RequestImpl request = new RequestImpl();
			request.setMethod(method);
			request.setService(clazz);
			request.setHost(beanDefinition.getAttribute("host") + "/summer-cloud/invoke");
			request.setArgs(args);
			Response invoke = filterChainInvoker.invoke(request);
			return invoke.getResult();
		};
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
	}


	public static Invoker getProvider(Object target, FilterManager filterManager) {
		Invoker invoker = new Invoker() {

			@SneakyThrows
			@Override
			public Response invoke(Request request) {
				ResponseImpl response = new ResponseImpl();
				Object invoke = request.getMethod().invoke(target, request.getArgs());
				response.setResult(invoke);
				return response;
			}
		};
		return buildFilterChain(invoker, filterManager.getProviderFilter());

	}


	private static Invoker buildFilterChain(Invoker invoker, List<Filter> filterList) {
		if (filterList.isEmpty()) {
			return invoker;
		}
		Invoker next = invoker;
		for (int i = filterList.size() - 1; i >= 0; i--) {
			next = new FilterChainInvoker(filterList.get(i), next);
		}
		return next;
	}

	@Override
	public T getObject() {
		return getConsumer();
	}

	@Override
	public Class<?> getObjectType() {
		return clazz;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.app = applicationContext;
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}
}
