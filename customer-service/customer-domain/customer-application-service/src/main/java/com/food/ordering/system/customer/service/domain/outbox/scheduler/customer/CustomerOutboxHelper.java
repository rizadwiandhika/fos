package com.food.ordering.system.customer.service.domain.outbox.scheduler.customer;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerEventPayload;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerOutboxHelper {

	private final CustomerOutboxRepository outboxRepo;
	private final ObjectMapper objectMapper;

	public CustomerOutboxHelper(CustomerOutboxRepository outboxRepo, ObjectMapper objectMapper) {
		this.outboxRepo = outboxRepo;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<List<CustomerOutboxMessage>> getCustomerOutboxByOutboxStatus(OutboxStatus outboxStatus) {
		return outboxRepo.findByOutboxStatus(outboxStatus);
	}

	@Transactional
	public void createCustomerOutbox(CustomerEventPayload payload) {
		CustomerOutboxMessage outboxMessage = CustomerOutboxMessage.builder()
				.id(UUID.randomUUID())
				.outboxStatus(OutboxStatus.STARTED)
				.payload(getPayload(payload))
				.createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
				.build();

		save(outboxMessage);
	}

	@Transactional
	public void updateCustomerOutbox(CustomerOutboxMessage outbox, OutboxStatus outboxStatus) {
		outbox.setOutboxStatus(outboxStatus);
		outbox.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
		save(outbox);
	}

	private void save(CustomerOutboxMessage outboxMessage) {
		CustomerOutboxMessage result = outboxRepo.save(outboxMessage);
		if (result == null) {
			log.error("Unable to persist CustomerOutboxMessage");
			throw new CustomerDomainException("Unable converting CustomerEventPayload into JSON");
		}
	}

	private String getPayload(CustomerEventPayload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			log.error("Unable converting CustomerEventPayload into JSON", e);
			throw new CustomerDomainException("Unable converting CustomerEventPayload into JSON");
		}
	}

}
