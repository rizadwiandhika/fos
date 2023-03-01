package com.food.ordering.system.customer.service.domain.outbox.model.customer;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CustomerEventPayload {

	private final UUID id;
	private final String username;
	private final String firstname;
	private final String lastName;

}
