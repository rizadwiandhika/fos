package com.food.ordering.system.customer.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.customer.service.domain.dto.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.outbox.scheduler.customer.CustomerOutboxHelper;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;

@Component
public class CreateCustomerCommandHandler {

	private final CustomerOutboxHelper customerOutboxHelper;
	private final CustomerRepository customerRepository;
	private final CustomerDomainService customerDomainService;
	private final CustomerDataMapper mapper;

	public CreateCustomerCommandHandler(CustomerOutboxHelper customerOutboxHelper,
			CustomerRepository customerRepository, CustomerDomainService customerDomainService,
			CustomerDataMapper mapper) {
		this.customerOutboxHelper = customerOutboxHelper;
		this.customerRepository = customerRepository;
		this.customerDomainService = customerDomainService;
		this.mapper = mapper;
	}

	@Transactional
	public CreateCustomerResponse createCustomer(CreateCustomerCommand request) {
		CustomerCreatedEvent event = customerDomainService
				.createCustomer(mapper.createCustomerCommandToCustomer(request));

		customerRepository.create(event.getCustomer());
		customerOutboxHelper.createCustomerOutbox(mapper.customerCreatedEventToCustomerEventPayload(event));

		return mapper.customerCreatedEventToCreateCustomerResponse(event, "Customer successfully created");
	}

}
