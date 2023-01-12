package org.summer.cloud.config;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.summer.cloud.config.client.SummerCloudConfigClient;
import org.summer.cloud.config.client.SummerCloudConfigClientFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author shenfeng
 */
public class SummerCloudConfigDataLocationResolver implements ConfigDataLocationResolver<SummerCloudConfigDataResource> {

    private final String PREFIX = "summer-cloud:";

    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        return location.hasPrefix(PREFIX);
    }

    @Override
    public List<SummerCloudConfigDataResource> resolve(ConfigDataLocationResolverContext context,
                                                       ConfigDataLocation location)
            throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        String fileName = location.getNonPrefixedValue(PREFIX);
        SummerCloudConfigDataResource summerCloudConfigDataResource = new SummerCloudConfigDataResource();
        summerCloudConfigDataResource.setFileName(fileName);
        SummerCloudConfigProperties summerCloudConfigProperties = loadProperties(context);


        ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
        if (!bootstrapContext.isRegistered(SummerCloudConfigClient.class)) {
            bootstrapContext.register(SummerCloudConfigClient.class,
                    c -> SummerCloudConfigClientFactory.build(summerCloudConfigProperties));
        }


        return List.of(summerCloudConfigDataResource);
    }

    protected SummerCloudConfigProperties loadProperties(
            ConfigDataLocationResolverContext context) {
        Binder binder = context.getBinder();
        BindHandler bindHandler = getBindHandler(context);

        SummerCloudConfigProperties summerCloudConfigProperties;
        if (context.getBootstrapContext().isRegistered(SummerCloudConfigProperties.class)) {
            summerCloudConfigProperties = context.getBootstrapContext()
                    .get(SummerCloudConfigProperties.class);
        } else {
            summerCloudConfigProperties = binder
                    .bind("summer.cloud.config", Bindable.of(SummerCloudConfigProperties.class),
                            bindHandler)
                    .map(properties -> binder
                            .bind(SummerCloudConfigProperties.PREFIX,
                                    Bindable.ofInstance(properties), bindHandler)
                            .orElse(properties))
                    .orElseGet(() -> binder
                            .bind(SummerCloudConfigProperties.PREFIX,
                                    Bindable.of(SummerCloudConfigProperties.class), bindHandler)
                            .orElseGet(SummerCloudConfigProperties::new));
        }

        return summerCloudConfigProperties;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }
//    @Override
//    public List<SummerCloudConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext context,
//             ConfigDataLocation location,
//            Profiles profiles) throws ConfigDataLocationNotFoundException {
//        return Collections.emptyList();
//    }
}
