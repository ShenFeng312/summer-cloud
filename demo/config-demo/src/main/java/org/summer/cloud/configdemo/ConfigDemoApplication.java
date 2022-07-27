package org.summer.cloud.configdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigDemoApplication implements CommandLineRunner {
	@Value("${aa}")
	String aa;
	@Value("${bb}")
	String bb;

	public static void main(String[] args) {
		SpringApplication.run(ConfigDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(aa);
		System.out.println(bb);
	}
}
