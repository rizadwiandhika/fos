package com.food.ordering.system.order.service.dataaccess.restaurant.exception;

public class RestaurantDataAccessException extends RuntimeException {

	public RestaurantDataAccessException() {
	}

	public RestaurantDataAccessException(String message) {
		super(message);
	}

	public RestaurantDataAccessException(Throwable cause) {
		super(cause);
	}

}
