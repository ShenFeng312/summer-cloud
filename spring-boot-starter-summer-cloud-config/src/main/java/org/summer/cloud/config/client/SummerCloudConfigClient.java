package org.summer.cloud.config.client;

import org.springframework.web.client.RestTemplate;
import org.summer.cloud.config.SummerCloudConfigProperties;

/**
 * @author shenfeng
 */
public class SummerCloudConfigClient {

    SummerCloudConfigProperties summerCloudConfigProperties;

    public SummerCloudConfigClient(SummerCloudConfigProperties summerCloudConfigProperties) {
        this.restTemplate = new RestTemplate();
        this.summerCloudConfigProperties = summerCloudConfigProperties;
    }

    RestTemplate restTemplate;

    public String getConfig(String fileName) {
        return restTemplate.getForObject(summerCloudConfigProperties.getUrl() + "/config/" + fileName, String.class);
    }

    public int getVersion(String fileName) {
        return restTemplate.getForObject(summerCloudConfigProperties.getUrl() + "/version/" + fileName, Integer.class);
    }
}
