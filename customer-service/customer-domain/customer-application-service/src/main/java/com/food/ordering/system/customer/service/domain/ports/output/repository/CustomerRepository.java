package com.food.ordering.system.customer.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.domain.valueObject.CustomerId;

public interface CustomerRepository {

	public Customer create(Customer customer);

	public Optional<Customer> findById(CustomerId customerId);

}
