package com.food.ordering.system.payment.service.domain;

import static com.food.ordering.system.domain.valueObject.PaymentStatus.CANCELLED;
import static com.food.ordering.system.domain.valueObject.PaymentStatus.COMPLETED;
import static com.food.ordering.system.outbox.OutboxStatus.STARTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestHelper {

	private final PaymentDomainService paymentDomainService;
	private final PaymentDataMapper paymentDataMapper;
	private final PaymentRepository paymentRepository;
	private final CreditEntryRepository creditEntryRepository;
	private final CreditHistoryRepository creditHistoryRepository;
	private final OrderOutboxHelper orderOutboxHelper;
	private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

	public PaymentRequestHelper(PaymentDomainService paymentDomainService, PaymentDataMapper paymentDataMapper,
			PaymentRepository paymentRepository,
			CreditEntryRepository creditEntryRepository, CreditHistoryRepository creditHistoryRepository,
			OrderOutboxHelper orderOutboxHelper, PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
		this.paymentDomainService = paymentDomainService;
		this.paymentDataMapper = paymentDataMapper;
		this.paymentRepository = paymentRepository;
		this.creditEntryRepository = creditEntryRepository;
		this.creditHistoryRepository = creditHistoryRepository;
		this.orderOutboxHelper = orderOutboxHelper;
		this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
	}

	@Transactional
	public void persistPayment(PaymentRequest paymentRequest) {
		// In case order service failed to process the payment-reponse-topic message
		// order service might ask for payment processing again for the same saga id.
		// So here, we check if the saga id had been processed, then
		// just publish the event directly, without reprocessing the payment
		if (publishIfOutboxMessageProcessedForPayment(paymentRequest, COMPLETED)) {
			log.info("Payment has been processed for saga id: {}", paymentRequest.getSagaId());
			return;
		}

		log.info("Persisting payment request for order id: {}", paymentRequest.getOrderId());

		Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistories(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<String>();

		PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(
				payment,
				creditEntry,
				creditHistories,
				failureMessages);

		// Persist changes to the DB, and save to the outbox table
		persistDBObject(payment, creditEntry, creditHistories, failureMessages);
		// orderOutboxRepository.save(paymentDataMapper
		// .paymentEventToOrderOutboxMessage(UUID.fromString(paymentRequest.getSagaId()),
		// paymentEvent));

		orderOutboxHelper.saveOrderOutboxMessage(
				paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(),
				STARTED,
				UUID.fromString(paymentRequest.getSagaId()));

	}

	@Transactional
	public void persistCancelPayment(PaymentRequest paymentRequest) {
		// In case order service failed to process the payment-reponse-topic message
		// order service might ask for payment processing again for the same saga id.
		// So here, we check if the saga id had been processed, then
		// just publish the event directly, without reprocessing the payment
		if (publishIfOutboxMessageProcessedForPayment(paymentRequest, CANCELLED)) {
			log.info("Payment has been processed for saga id: {}", paymentRequest.getSagaId());
			return;
		}

		log.info("Cancelling payment request for order id: {}", paymentRequest.getOrderId());

		Optional<Payment> paymentOptional = paymentRepository
				.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));

		if (paymentOptional.isEmpty()) {
			String message = String.format("No payment found for order id: %s", paymentRequest.getOrderId());
			log.error(message);
			throw new PaymentNotFoundException(message);
		}

		Payment payment = paymentOptional.get();
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistories(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<String>();

		PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(
				payment,
				creditEntry,
				creditHistories,
				failureMessages);

		persistDBObject(payment, creditEntry, creditHistories, failureMessages);

		// No need for optimistic locking, since in this step,
		// we just create a new record. The record constrainted with unique
		// type, saga_id, payment_status,outbox_status.
		// If another thread attempts to create the same (type, saga_id, payment_status,
		// outbox_status) outbox message, the database will reject that.
		// So there would be no duplicate event saved in the outbox table
		orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(), STARTED, UUID.fromString(paymentRequest.getSagaId()));
	}

	private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest,
			PaymentStatus paymentStatus) {
		Optional<OrderOutboxMessage> op = orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
				UUID.fromString(paymentRequest.getSagaId()),
				paymentStatus);

		if (op.isPresent()) {
			paymentResponseMessagePublisher.publish(op.get(), orderOutboxHelper::updatePaymentOutboxMessage);
			return true;
		}

		return false;
	}

	private void persistDBObject(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories,
			List<String> failureMessages) {
		paymentRepository.save(payment);

		if (failureMessages.isEmpty()) {
			creditEntryRepository.save(creditEntry);
			creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
		}
	}

	private List<CreditHistory> getCreditHistories(CustomerId customerId) {
		Optional<List<CreditHistory>> creditHistory = creditHistoryRepository.findByCustomerId(customerId);
		if (creditHistory.isEmpty()) {
			String message = String.format("No credit history found for credit entry id: %s",
					customerId.getValue().toString());
			log.warn(message);
			throw new PaymentApplicationServiceException(message);
		}

		return creditHistory.get();
	}

	private CreditEntry getCreditEntry(CustomerId customerId) {
		Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
		if (creditEntry.isEmpty()) {
			String message = String.format("No credit entry found for customer id: %s",
					customerId.getValue().toString());
			log.warn(message);
			throw new PaymentApplicationServiceException(message);
		}
		return creditEntry.get();
	}

}
