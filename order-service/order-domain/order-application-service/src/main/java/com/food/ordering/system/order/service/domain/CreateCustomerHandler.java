package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.message.CustomerMessage;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateCustomerHandler {

	private final CustomerRepository customerRepository;
	private final OrderDataMapper orderDataMapper;

	public CreateCustomerHandler(CustomerRepository customerRepository, OrderDataMapper orderDataMapper) {
		this.customerRepository = customerRepository;
		this.orderDataMapper = orderDataMapper;
	}

	@Transactional
	public void persistCustomer(CustomerMessage message) {
		Customer customer = null;

		try {
			customer = customerRepository.save(orderDataMapper.customerMessageToCustomer(message));
		} catch (Exception e) {
			// log.error(e.);
			log.error(e.getMessage());
			log.error("Error while persisting customer", e);
			throw e;
		}

		if (customer == null) {
			log.error("Unable to persist customer: {}", message.getUsername());
			throw new OrderDomainException("Unable to persist customer: " + message.getUsername());
		}

		log.info("Customer created: {}", message.getUsername());
	}

}
