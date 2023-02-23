package com.food.ordering.system.customer.service.dataaccess.outbox.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.dataaccess.outbox.exception.CustomerOutboxNotFoundException;
import com.food.ordering.system.customer.service.dataaccess.outbox.mapper.CustomerOutboxDataAccessMapper;
import com.food.ordering.system.customer.service.dataaccess.outbox.repository.CustomerOutboxJpaRepository;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;

@Component
public class CustomerOutboxRepositoryImpl implements CustomerOutboxRepository {

	private final CustomerOutboxJpaRepository jpaRepo;
	private final CustomerOutboxDataAccessMapper mapper;

	public CustomerOutboxRepositoryImpl(CustomerOutboxJpaRepository jpaRepo, CustomerOutboxDataAccessMapper mapper) {
		this.jpaRepo = jpaRepo;
		this.mapper = mapper;
	}

	@Override
	public Optional<List<CustomerOutboxMessage>> findByOutboxStatus(OutboxStatus outboxStatus) {
		return Optional.of(jpaRepo.findByOutboxStatus(outboxStatus)
				.orElseThrow(() -> new CustomerOutboxNotFoundException("Customer outbox not found for: " + outboxStatus.name()))
				.stream()
				.map(mapper::CustomerOutboxEntityToCustomerOutboxMessage)
				.collect(Collectors.toList()));
	}

	@Override
	public CustomerOutboxMessage save(CustomerOutboxMessage outbox) {
		return mapper.CustomerOutboxEntityToCustomerOutboxMessage(
				jpaRepo.save(mapper.customerOutboxMessageToCustomerOutboxEntity(outbox)));
	}

}
