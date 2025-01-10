package com.example.LogApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogAppApplication.class, args);
	}

}
