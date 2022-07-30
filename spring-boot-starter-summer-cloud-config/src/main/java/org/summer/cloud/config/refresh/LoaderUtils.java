package org.summer.cloud.config.refresh;

import lombok.SneakyThrows;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;
import org.summer.cloud.common.entity.ConfigEntity;

import java.util.Arrays;
import java.util.List;

/**
 * @author shenfeng
 */
public class LoaderUtils {
    private static final String DEFAULT_EXTENSION = "properties";
    private static final String DOT = ".";
    public static boolean canLoadFileExtension(PropertySourceLoader loader, String extension) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(extension,
                        fileExtension));
    }


    public static String getFileExtension(String name) {
        if (!StringUtils.hasLength(name)) {
            return DEFAULT_EXTENSION;
        }
        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1) {
            return name.substring(idx + 1);
        }
        return DEFAULT_EXTENSION;
    }

    @SneakyThrows
    public static List<PropertySource<?>> getConfigData(ConfigEntity configEntity,
                                                        List<PropertySourceLoader> propertySourceLoaders)  {
        if (configEntity != null) {
            ByteArrayResource byteArrayResource = new ByteArrayResource(configEntity.getConfig().getBytes(),
                    configEntity.getFileName());
            for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {

                if (LoaderUtils.canLoadFileExtension(propertySourceLoader,
                        LoaderUtils.getFileExtension(configEntity.getFileName()))) {

                    List<PropertySource<?>> load = propertySourceLoader.load(configEntity.getFileName(), byteArrayResource);
                    return load;

                }
            }
        }
        return null;
    }
}
