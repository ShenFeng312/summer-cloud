package org.summer.cloud.rpc;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author shenfeng
 */
public interface Request {
	Set<String> getHeaderNames();

	String getHeader(String key);

	void addHeader(String key, String value);

	void removeHeader(String key);

	Object[] getArgs();

	Class<?> getService();

	String getHost();

	Method getMethod();

}
