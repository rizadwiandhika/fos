package com.food.ordering.system.customer.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.customer.service.domain.dto.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;

@Service
public class CustomerApplicationServiceImpl implements CustomerApplicationService {

	private final CreateCustomerCommandHandler createCustomerCommandHandler;

	public CustomerApplicationServiceImpl(CreateCustomerCommandHandler createCustomerCommandHandler) {
		this.createCustomerCommandHandler = createCustomerCommandHandler;
	}

	@Override
	public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
		return createCustomerCommandHandler.createCustomer(createCustomerCommand);
	}

}
