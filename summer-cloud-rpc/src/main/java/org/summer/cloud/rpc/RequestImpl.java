package org.summer.cloud.rpc;

import org.summer.cloud.rpc.Request;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author shenfeng
 */
public class RequestImpl implements Request {

	private final Map<String,String> headers = new HashMap<>();

	private Object[] args;

	private Class<?> service;

	private String host;
	private Method method;

	@Override
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public void addHeader(String key, String value) {
		headers.put(key,value);
	}

	@Override
	public void removeHeader(String key) {
		headers.remove(key);
	}

	@Override
	public Object[] getArgs() {
		return args;
	}

	@Override
	public Class<?> getService() {
		return service;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public Method getMethod() {
		return method;
	}


	public void setArgs(Object[] args) {
		this.args = args;
	}

	public void setService(Class<?> service) {
		this.service = service;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
