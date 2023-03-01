package com.food.ordering.system.customer.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {

	private String username;
	private String firstName;
	private String lastName;

	public Customer(CustomerId customerId, String username, String firstName, String lastName) {
		setId(customerId);
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

}
