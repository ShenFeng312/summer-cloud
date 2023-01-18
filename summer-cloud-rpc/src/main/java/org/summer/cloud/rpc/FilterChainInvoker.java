package org.summer.cloud.rpc;

/**
 * @author shenfeng
 */
public class FilterChainInvoker implements Invoker {
	private final Invoker next;
	private final Filter curr;

	public FilterChainInvoker(Filter curr, Invoker next) {
		this.curr = curr;
		this.next = next;
	}

	@Override
	public Response invoke(Request request) {
		return curr.invoke(next, request);

	}
}
