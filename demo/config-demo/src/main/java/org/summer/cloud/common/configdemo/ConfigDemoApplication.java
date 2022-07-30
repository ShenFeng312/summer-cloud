package org.summer.cloud.common.configdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * @author shenfeng
 */
@SpringBootApplication
@Slf4j
public class ConfigDemoApplication implements EnvironmentAware, InitializingBean {


	@Resource
	private BeanB beanB;
	private Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(ConfigDemoApplication.class, args);
	}



	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void afterPropertiesSet() {
		new Thread(()->{
			while (true){
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.out.println(environment.getProperty("aa"));
				System.out.println(beanB.getAa());
			}

		}).start();
	}
}
