package org.summer.cloud.rpc;

import java.util.List;

public interface FilterManager {
	List<Filter> getConsumerFilter();
	List<Filter> getProviderFilter();
	void register(Filter filter);
}
