package org.summer.cloud.rpc;

import lombok.Setter;

@Setter
public class ResponseImpl implements Response {
	private Object result;
	private Throwable throwable;

	@Override
	public Object getResult() {
		return this.result;
	}

	@Override
	public Throwable getThrowable() {
		return this.throwable;
	}


}
