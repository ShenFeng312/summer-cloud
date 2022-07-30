package org.summer.cloud.config.refresh;

import org.springframework.context.ApplicationEvent;


/**
 * @author shenfeng
 */
public class CloudSummerEnvRefreshEvent extends ApplicationEvent {
    public CloudSummerEnvRefreshEvent(Object source) {
        super(source);
    }

}
