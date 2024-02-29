package com.overcomingroom.bellbell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BellbellApplication {

	public static void main(String[] args) {
		SpringApplication.run(BellbellApplication.class, args);
	}

}
