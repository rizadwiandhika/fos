package com.food.ordering.system.customer.service.domain.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.domain.dto.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerEventPayload;
import com.food.ordering.system.domain.valueObject.CustomerId;

@Component
public class CustomerDataMapper {

	public Customer createCustomerCommandToCustomer(CreateCustomerCommand createCustomerCommand) {
		return new Customer(new CustomerId(UUID.randomUUID()), createCustomerCommand.getUsername(),
				createCustomerCommand.getFirstName(), createCustomerCommand.getLastName());
	}

	public CreateCustomerResponse customerCreatedEventToCreateCustomerResponse(CustomerCreatedEvent event,
			String message) {
		return new CreateCustomerResponse(event.getCustomer().getUsername(), message);
	}

	public CustomerEventPayload customerCreatedEventToCustomerEventPayload(CustomerCreatedEvent event) {
		return CustomerEventPayload.builder()
				.id(event.getCustomer().getId().getValue())
				.username(event.getCustomer().getUsername())
				.firstname(event.getCustomer().getFirstName())
				.lastName(event.getCustomer().getLastName())
				.build();
	}

}
