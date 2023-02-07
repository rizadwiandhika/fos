package com.food.ordering.system.restaurant.service.domain.exception;

public class RestaurantNotFoundException extends RestaurantDomainException {

	public RestaurantNotFoundException(String message) {
		super(message);
	}

	public RestaurantNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
