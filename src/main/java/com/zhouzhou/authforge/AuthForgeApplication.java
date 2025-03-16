package com.zhouzhou.authforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.zhouzhou.authforge.config")
public class AuthForgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthForgeApplication.class, args);
	}

}
