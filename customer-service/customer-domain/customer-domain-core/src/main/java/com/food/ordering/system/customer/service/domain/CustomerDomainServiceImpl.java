package com.food.ordering.system.customer.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;

public class CustomerDomainServiceImpl implements CustomerDomainService {

	@Override
	public CustomerCreatedEvent createCustomer(Customer customer) {
		return new CustomerCreatedEvent(customer, ZonedDateTime.now(ZoneId.of(UTC)));
	}

}
