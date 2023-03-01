package com.food.ordering.system.customer.service.domain.ports.output.repository;

import java.util.List;
import java.util.Optional;

import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

public interface CustomerOutboxRepository {

	Optional<List<CustomerOutboxMessage>> findByOutboxStatus(OutboxStatus outboxStatus);

	CustomerOutboxMessage save(CustomerOutboxMessage outbox);

}
