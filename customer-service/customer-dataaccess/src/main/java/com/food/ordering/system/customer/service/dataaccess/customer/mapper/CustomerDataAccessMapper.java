package com.food.ordering.system.customer.service.dataaccess.customer.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.domain.valueObject.CustomerId;

@Component
public class CustomerDataAccessMapper {

	public CustomerEntity customerToCustomerEntity(Customer customer) {
		return CustomerEntity.builder()
				.id(customer.getId().getValue())
				.username(customer.getUsername())
				.firstName(customer.getFirstName())
				.lastName(customer.getLastName())
				.build();
	}

	public Customer customerEntityToCustomer(CustomerEntity entity) {
		return new Customer(new CustomerId(entity.getId()), entity.getUsername(), entity.getFirstName(),
				entity.getLastName());
	}

}
