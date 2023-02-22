package com.food.ordering.system.restaurant.service.dataaccess.outbox.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.dataaccess.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.restaurant.service.dataaccess.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;

@Component
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

	private final OrderOutboxJpaRepository orderOutboxJpaRepository;
	private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

	public OrderOutboxRepositoryImpl(OrderOutboxJpaRepository orderOutboxJpaRepository,
			OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
		this.orderOutboxJpaRepository = orderOutboxJpaRepository;
		this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
	}

	@Override
	public OrderOutboxMessage save(OrderOutboxMessage orderPaymentOuthoxMessage) {
		return orderOutboxDataAccessMapper
				.orderOutboxEntityToOrderOutboxMessage(orderOutboxJpaRepository
						.save(orderOutboxDataAccessMapper
								.orderOutboxMessageToOutboxEntity(orderPaymentOuthoxMessage)));
	}

	@Override
	public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String sagaType, OutboxStatus outboxStatus) {
		return Optional.of(orderOutboxJpaRepository.findByTypeAndOutboxStatus(sagaType, outboxStatus)
				.orElseThrow(() -> new OrderOutboxNotFoundException("Order outbox not found"))
				.stream()
				.map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId,
			OutboxStatus outboxStatus) {
		return orderOutboxJpaRepository.findByTypeAndSagaIdAndOutboxStatus(type, sagaId, outboxStatus)
				.map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
	}

	@Override
	public void deleteByTypeAndoutboxStatus(String type, OutboxStatus outboxStatus) {
		orderOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
	}

}
