package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {

	private final String username;
	private final String firstName;
	private final String lastName;

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Customer(CustomerId customerId, String username, String firstName, String lastName) {
		setId(customerId);
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}

}
