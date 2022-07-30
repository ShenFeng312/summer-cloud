package org.summer.cloud.config.refresh;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.context.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.summer.cloud.common.entity.ConfigEntity;
import org.summer.cloud.config.client.SummerCloudConfigClient;
import org.summer.cloud.config.client.SummerCloudConfigClientFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author shenfeng
 */

public class SummerCloudRefresher implements Runnable, EnvironmentAware,
        ApplicationListener<ApplicationPreparedEvent> , ApplicationContextAware, DisposableBean {

    private static final Set<ConfigEntity> CONFIG_ENTITIES = new CopyOnWriteArraySet<>();

    List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader.
            loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    private final SummerCloudConfigClient client = SummerCloudConfigClientFactory.get();
    //todo ThreadFactory

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r,
            "summerCloudRefresher"));
    private ConfigurableEnvironment environment;
    private SpringApplication springApplication;
    private ConfigurableApplicationContext context;

    public void start() {
        executor.scheduleAtFixedRate(this, 10,10, TimeUnit.SECONDS);
    }


    @Override
    public void run() {

        CONFIG_ENTITIES.forEach(config->{
            int version = client.getVersion(config.getFileName());
            if(version> config.getVersion()){
                refresh(config.getFileName());
            }
            context.publishEvent(new CloudSummerEnvRefreshEvent(context));
        });
    }

    @SneakyThrows
    private void refresh(String fileName) {

        ConfigEntity config = client.getConfig(fileName);
        List<PropertySource<?>> configData = LoaderUtils.getConfigData(config, propertySourceLoaders);
        configData.forEach(item-> environment.getPropertySources().addLast(item));
    }

    public static  void registryConfigFile(ConfigEntity config) {
        CONFIG_ENTITIES.add(config);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        this.springApplication = event.getSpringApplication();
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }
}
