package org.summer.cloud.config.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.summer.cloud.common.entity.ConfigEntity;
import org.summer.cloud.config.SummerCloudConfigProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shenfeng
 */
public class SummerCloudConfigClient {
    SummerCloudConfigProperties summerCloudConfigProperties;
    ObjectMapper objectMapper = new ObjectMapper();

    private List<String> fileList = new ArrayList<>();

    protected SummerCloudConfigClient(SummerCloudConfigProperties summerCloudConfigProperties) {
        this.restTemplate = new RestTemplate();
        this.summerCloudConfigProperties = summerCloudConfigProperties;
    }

    RestTemplate restTemplate;

    @SneakyThrows
    public ConfigEntity getConfig(String fileName) {

        String config = restTemplate.getForObject(summerCloudConfigProperties.getUrl() + "/config/" + fileName,
                String.class);
        if(StringUtils.hasText(config)){
            return objectMapper.readValue(config, ConfigEntity.class);
        }
        return null;
    }

    public int getVersion(String fileName) {
        return restTemplate.getForObject(summerCloudConfigProperties.getUrl() + "/version/" + fileName, Integer.class);
    }
}
