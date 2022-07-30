package org.summer.cloud.config;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.summer.cloud.common.entity.ConfigEntity;
import org.summer.cloud.config.client.SummerCloudConfigClient;
import org.summer.cloud.config.refresh.SummerCloudRefresher;

import java.io.IOException;
import java.util.List;

import static org.summer.cloud.config.refresh.LoaderUtils.getConfigData;

/**
 * @author shenfeng
 */
public class SummerCloudConfigDataLoader implements ConfigDataLoader<SummerCloudConfigDataResource> {


    List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader.
            loadFactories(PropertySourceLoader.class, getClass().getClassLoader());

    @Override
    public ConfigData load(ConfigDataLoaderContext context, SummerCloudConfigDataResource resource)
            throws IOException, ConfigDataResourceNotFoundException {

        SummerCloudConfigClient summerCloudConfigClient =
                context.getBootstrapContext().get(SummerCloudConfigClient.class);



        ConfigEntity configEntity = summerCloudConfigClient.getConfig(resource.getFileName());
        SummerCloudRefresher.registryConfigFile(configEntity);
        List<PropertySource<?>> load = getConfigData(configEntity,propertySourceLoaders);
        if (load != null){
            return new ConfigData(load);
        }


        return null;
    }




}
