package com.food.ordering.system.customer.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.food.ordering.system.customer.service.dataaccess" })
@EntityScan(basePackages = { "com.food.ordering.system.customer.service.dataaccess" })
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class CustomerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

}
