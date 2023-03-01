package com.food.ordering.system.customer.service.domain.ports.input.service;

import com.food.ordering.system.customer.service.domain.dto.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.CreateCustomerResponse;

public interface CustomerApplicationService {

	CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand);

}
