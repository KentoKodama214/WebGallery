package com.web.gallary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebGallaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebGallaryApplication.class, args);
	}
}