package com.example.portfolio_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortfolioManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioManagementSystemApplication.class, args);
	}
}
