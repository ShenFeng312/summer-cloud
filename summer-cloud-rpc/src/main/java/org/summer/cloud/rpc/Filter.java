package org.summer.cloud.rpc;

public interface Filter {
	Response invoke(Invoker invoker, Request request);

	boolean isConsumer();

	boolean isProvider();
}
