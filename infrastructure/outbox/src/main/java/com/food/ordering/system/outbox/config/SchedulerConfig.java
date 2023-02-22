package com.food.ordering.system.outbox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Enable scheduling
public class SchedulerConfig {

	// Create any specific data mapper here (e.g for JSON) or customize any bean
	// But for now, we will use the default ObjectMapper that is provided by
	// Spring Boot Starter Json
	// e.g:
	// @Bean
	// public ObjectMapper objectMapper() {
	// return new
	// ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
	//
	// }

}
