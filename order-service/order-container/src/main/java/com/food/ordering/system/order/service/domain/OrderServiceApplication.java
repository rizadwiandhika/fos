package com.food.ordering.system.order.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {
		"com.food.ordering.system.order.service.dataaccess",
		"com.food.ordering.system.dataaccess" }) // Hanya JPA class yang ada
													// pada package ini yang
													// akan discan
@EntityScan(basePackages = { "com.food.ordering.system.order.service.dataaccess",
		"com.food.ordering.system.dataaccess" }) // Hanya Entity pada package ini yang
// akan discan untuk JPA Entities
@SpringBootApplication(scanBasePackages = "com.food.ordering.system") // Semua package yang berawalan
																		// "com.food.ordering.system" akan bisa dipakai
public class OrderServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}
}
