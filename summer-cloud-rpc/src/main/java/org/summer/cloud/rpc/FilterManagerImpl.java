package org.summer.cloud.rpc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shenfeng
 */
public class FilterManagerImpl implements FilterManager, ApplicationContextAware {
	private final List<Filter> consumerFilters = new ArrayList<>();
	private final List<Filter> providerFilters = new ArrayList<>();

	@Override
	public List<Filter> getConsumerFilter() {
		return consumerFilters;
	}

	@Override
	public List<Filter> getProviderFilter() {
		return providerFilters;
	}

	@Override
	public void register(Filter filter) {
		if (filter.isConsumer()) {
			consumerFilters.add(filter);
		}
		if (filter.isProvider()) {
			providerFilters.add(filter);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, Filter> filterMap = applicationContext.getBeansOfType(Filter.class);
		filterMap.values().forEach(this::register);
	}
}
