package com.guegue.duty_checker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DutyCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DutyCheckerApplication.class, args);
	}

}
