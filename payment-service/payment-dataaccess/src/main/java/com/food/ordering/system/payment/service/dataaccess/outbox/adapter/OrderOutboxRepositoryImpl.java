package com.food.ordering.system.payment.service.dataaccess.outbox.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.payment.service.dataaccess.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;

@Component
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

	private final OrderOutboxJpaRepository jpaRepo;
	private final OrderOutboxDataAccessMapper mapper;

	public OrderOutboxRepositoryImpl(OrderOutboxJpaRepository jpaRepo, OrderOutboxDataAccessMapper mapper) {
		this.jpaRepo = jpaRepo;
		this.mapper = mapper;
	}

	@Override
	public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
		jpaRepo.deleteByTypeAndOutboxStatus(type, outboxStatus);
	}

	@Override
	public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
		return Optional.of(jpaRepo.findByTypeAndOutboxStatus(type, outboxStatus)
				.orElseThrow(() -> new OrderOutboxNotFoundException("Order outbox not found"))
				.stream()
				.map(mapper::orderOutboxEntityToOrderOutboxMessage)
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(String type, UUID sagaId,
			PaymentStatus paymentStatus, OutboxStatus outboxStatus) {
		return jpaRepo.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(type, sagaId, paymentStatus, outboxStatus)
				.map(mapper::orderOutboxEntityToOrderOutboxMessage);
	}

	@Override
	public OrderOutboxMessage save(OrderOutboxMessage outbox) {
		return mapper
				.orderOutboxEntityToOrderOutboxMessage(
						jpaRepo.save(mapper.orderOutboxMessageToOutboxEntity(outbox)));
	}

}
