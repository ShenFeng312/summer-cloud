package org.summer.cloud.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


import static org.summer.cloud.config.SummerCloudConfigProperties.PREFIX;

/**
 * @author shenfeng
 */
@ConfigurationProperties(PREFIX)
@Getter
@Setter
public class SummerCloudConfigProperties {

    public static final String PREFIX = "summer.cloud.config";

    private String url;
//    private List<String> configFiles;
}
