package com.food.ordering.system.customer.service.domain.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCustomerResponse {

	@NotNull
	private final String username;

	@NotNull
	private final String message;

}
