package com.food.ordering.system.customer.service.dataaccess.outbox.exception;

public class CustomerOutboxNotFoundException extends RuntimeException {

	public CustomerOutboxNotFoundException(String message) {
		super(message);
	}

	public CustomerOutboxNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
