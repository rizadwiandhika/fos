package com.food.ordering.system.order.service.domain.dto.message;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CustomerMessage {

	private final UUID id;
	private final String username;
	private final String firstName;
	private final String lastName;

}
