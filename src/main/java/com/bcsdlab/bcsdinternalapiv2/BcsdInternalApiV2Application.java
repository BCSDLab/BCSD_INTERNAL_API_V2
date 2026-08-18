package com.bcsdlab.bcsdinternalapiv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BcsdInternalApiV2Application {

	public static void main(String[] args) {
		SpringApplication.run(BcsdInternalApiV2Application.class, args);
	}

}
