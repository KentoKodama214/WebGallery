package com.web.gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebGalleryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebGalleryApplication.class, args);
	}
}