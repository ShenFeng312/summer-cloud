package org.summer.cloud.rpc;

/**
 * @author shenfeng
 */
public interface Invoker {
	/**
	 * 发起请求
	 * @param request request
	 * @return result
	 */
	Response invoke(Request request);
}
