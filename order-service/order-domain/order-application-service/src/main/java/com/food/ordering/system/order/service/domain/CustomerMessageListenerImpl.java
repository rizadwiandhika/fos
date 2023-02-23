package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.dto.message.CustomerMessage;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;

@Component
public class CustomerMessageListenerImpl implements CustomerMessageListener {

	private final CreateCustomerHandler createCustomerHandler;

	public CustomerMessageListenerImpl(CreateCustomerHandler createCustomerHandler) {
		this.createCustomerHandler = createCustomerHandler;
	}

	@Override
	public void createCustomer(CustomerMessage message) {
		createCustomerHandler.persistCustomer(message);
	}

}
