package org.summer.cloud.rpc;

/**
 * @author shenfeng
 */
public interface FilterChain {
	/**
	 *
	 * @param invoker invoker
	 * @param request request
	 * @return result
	 */
	Response invoke(Invoker invoker,Request request);
}
