package com.example.enarm360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Enarm360Application {

	public static void main(String[] args) {
		SpringApplication.run(Enarm360Application.class, args);
	}

}


