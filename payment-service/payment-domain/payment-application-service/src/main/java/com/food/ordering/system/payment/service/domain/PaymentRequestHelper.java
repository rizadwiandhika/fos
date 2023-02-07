package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
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
	private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
	private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;
	private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;

	public PaymentRequestHelper(PaymentDomainService paymentDomainService, PaymentDataMapper paymentDataMapper,
			PaymentRepository paymentRepository, CreditEntryRepository creditEntryRepository,
			CreditHistoryRepository creditHistoryRepository,
			PaymentCompletedMessagePublisher paymentCompletedMessagePublisher,
			PaymentCancelledMessagePublisher paymentCancelledMessagePublisher,
			PaymentFailedMessagePublisher paymentFailedMessagePublisher) {
		this.paymentDomainService = paymentDomainService;
		this.paymentDataMapper = paymentDataMapper;
		this.paymentRepository = paymentRepository;
		this.creditEntryRepository = creditEntryRepository;
		this.creditHistoryRepository = creditHistoryRepository;
		this.paymentCompletedMessagePublisher = paymentCompletedMessagePublisher;
		this.paymentCancelledMessagePublisher = paymentCancelledMessagePublisher;
		this.paymentFailedMessagePublisher = paymentFailedMessagePublisher;
	}

	@Transactional
	public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
		log.info("Persisting payment request for order id: {}", paymentRequest.getOrderId());

		Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistories(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<String>();

		PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(
				payment,
				creditEntry,
				creditHistories,
				failureMessages,
				paymentCompletedMessagePublisher,
				paymentFailedMessagePublisher);

		persistDBObject(payment, creditEntry, creditHistories, failureMessages);

		return paymentEvent;
	}

	@Transactional
	public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
		log.info("Cancelling payment request for order id: {}", paymentRequest.getOrderId());

		Optional<Payment> paymentOptional = paymentRepository
				.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));

		if (paymentOptional.isEmpty()) {
			String message = String.format("No payment found for order id: %s", paymentRequest.getOrderId());
			log.error(message);
			throw new PaymentApplicationServiceException(message);
		}

		Payment payment = paymentOptional.get();
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistories(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<String>();

		PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(
				payment,
				creditEntry,
				creditHistories,
				failureMessages,
				paymentCancelledMessagePublisher,
				paymentFailedMessagePublisher);

		persistDBObject(payment, creditEntry, creditHistories, failureMessages);

		return paymentEvent;
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
