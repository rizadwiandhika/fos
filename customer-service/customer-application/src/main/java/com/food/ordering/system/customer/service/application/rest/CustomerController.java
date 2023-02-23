package com.food.ordering.system.customer.service.application.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.ordering.system.customer.service.domain.dto.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/customers", produces = "application/vnd.api.v1+json")
public class CustomerController {

	private final CustomerApplicationService customerApplicationService;

	public CustomerController(CustomerApplicationService customerApplicationService) {
		this.customerApplicationService = customerApplicationService;
	}

	@PostMapping
	public ResponseEntity<CreateCustomerResponse> create(@RequestBody CreateCustomerCommand request) {
		log.info("Receiving create customer request");

		CreateCustomerResponse response = customerApplicationService.createCustomer(request);

		log.info(response.getMessage());
		return ResponseEntity.ok(response);
	}

}
