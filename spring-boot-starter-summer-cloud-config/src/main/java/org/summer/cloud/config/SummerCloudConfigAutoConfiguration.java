package org.summer.cloud.config;

import org.springframework.context.annotation.Bean;
import org.summer.cloud.config.refresh.SummerCloudRefreshScope;
import org.summer.cloud.config.refresh.SummerCloudRefresher;

/**
 * @author shenfeng
 */

public class SummerCloudConfigAutoConfiguration {

    @Bean
    public SummerCloudRefreshScope refreshScope() {
        return new SummerCloudRefreshScope();
    }

    @Bean(initMethod = "start")
    public SummerCloudRefresher summerCloudRefresher() {
        return new SummerCloudRefresher();
    }

}
