package com.food.ordering.system.order.service.dataaccess.outbox.payment.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

	private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
	private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

	public PaymentOutboxRepositoryImpl(PaymentOutboxJpaRepository paymentOutboxJpaRepository,
			PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
		this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
		this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
	}

	@Override
	public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		paymentOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus,
				Arrays.asList(sagaStatus));
	}

	@Override
	public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type,
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		String err = "Payment outbox not found for type: " + type
				+ " and outbox status: " + outboxStatus + " and saga status: "
				+ String.join(",",
						Arrays.asList(sagaStatus).stream().map(SagaStatus::name).collect(Collectors.toList()));

		List<OrderPaymentOutboxMessage> result = paymentOutboxJpaRepository
				.findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus))
				.orElseThrow(() -> new PaymentOutboxNotFoundException(err))
				.stream()
				.map(paymentOutboxDataAccessMapper::outboxEntityToOrderPaymentOutboxMessage)
				.collect(java.util.stream.Collectors.toList());

		return Optional.of(result);
	}

	@Override
	public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId,
			SagaStatus... sagaStatus) {
		return paymentOutboxJpaRepository
				.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
				.map(paymentOutboxDataAccessMapper::outboxEntityToOrderPaymentOutboxMessage);

	}

	@Override
	public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
		PaymentOutboxEntity entity = paymentOutboxDataAccessMapper
				.orderPaymentOutboxMessagToOutboxEntity(orderPaymentOutboxMessage);

		PaymentOutboxEntity savedEntity = paymentOutboxJpaRepository.save(entity);

		return paymentOutboxDataAccessMapper.outboxEntityToOrderPaymentOutboxMessage(savedEntity);
	}

}
