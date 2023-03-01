package com.food.ordering.system.customer.service.dataaccess.customer.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.customer.service.dataaccess.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.domain.valueObject.CustomerId;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

	private final CustomerJpaRepository repo;
	private final CustomerDataAccessMapper mapper;

	public CustomerRepositoryImpl(CustomerJpaRepository repo, CustomerDataAccessMapper mapper) {
		this.repo = repo;
		this.mapper = mapper;
	}

	@Override
	public Customer create(Customer customer) {
		return mapper.customerEntityToCustomer(repo.save(mapper.customerToCustomerEntity(customer)));
	}

	@Override
	public Optional<Customer> findById(CustomerId customerId) {
		return repo.findById(customerId.getValue()).map(mapper::customerEntityToCustomer);
	}

}
