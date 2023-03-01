package com.food.ordering.system.customer.service.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "customer-service")
public class CustomerServiceConfig {

	private String customerTopicName;

}
