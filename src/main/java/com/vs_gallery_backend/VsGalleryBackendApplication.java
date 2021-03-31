package com.vs_gallery_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "Controllers" } )

@SpringBootApplication
public class VsGalleryBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VsGalleryBackendApplication.class, args);
	}

}
