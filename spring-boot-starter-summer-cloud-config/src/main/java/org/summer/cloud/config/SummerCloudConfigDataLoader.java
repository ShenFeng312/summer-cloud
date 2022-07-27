package org.summer.cloud.config;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;
import org.summer.cloud.config.client.SummerCloudConfigClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author shenfeng
 */
public class SummerCloudConfigDataLoader implements ConfigDataLoader<SummerCloudConfigDataResource> {
    private static final String DEFAULT_EXTENSION = "properties";
    private static final String DOT = ".";
    List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader.
            loadFactories(PropertySourceLoader.class, getClass().getClassLoader());

    @Override
    public ConfigData load(ConfigDataLoaderContext context, SummerCloudConfigDataResource resource) throws IOException, ConfigDataResourceNotFoundException {
        SummerCloudConfigClient summerCloudConfigClient = context.getBootstrapContext().get(SummerCloudConfigClient.class);
        String config = summerCloudConfigClient.getConfig(resource.getFileName());
        ByteArrayResource byteArrayResource = new ByteArrayResource(config.getBytes(), resource.getFileName());
        for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {

            if (canLoadFileExtension(propertySourceLoader, getFileExtension(resource.getFileName()))) {

                List<PropertySource<?>> load = propertySourceLoader.load(null, byteArrayResource);
                return new ConfigData(load);

            }
        }

        return null;
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String extension) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(extension,
                        fileExtension));
    }


    public String getFileExtension(String name) {
        if (!StringUtils.hasLength(name)) {
            return DEFAULT_EXTENSION;
        }
        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1) {
            return name.substring(idx + 1);
        }
        return DEFAULT_EXTENSION;
    }

}
