package org.summer.cloud.rpc.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.summer.cloud.rpc.FilterManager;
import org.summer.cloud.rpc.FilterManagerImpl;

/**
 * @author shenfeng
 */
public class HttpRpcAutoConfiguration {
	@ConditionalOnMissingBean
	@Bean
	public SummerCloudResourceBeanDefinitionPostProcessor summerCloudResourceBeanDefinitionPostProcessor() {
		return new SummerCloudResourceBeanDefinitionPostProcessor();
	}

	@ConditionalOnMissingBean
	@Bean
	FilterManager filterManager() {
		return new FilterManagerImpl();
	}

	@ConditionalOnMissingBean
	@Bean
	public HttpRequestController httpRequestController() {
		return new HttpRequestController();
	}

}
