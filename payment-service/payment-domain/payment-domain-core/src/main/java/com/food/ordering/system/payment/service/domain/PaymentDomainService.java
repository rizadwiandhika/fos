package com.food.ordering.system.payment.service.domain;

import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;

public interface PaymentDomainService {
	PaymentEvent validateAndInitiatePayment(Payment payment,
			CreditEntry creditEntry,
			List<CreditHistory> creditHistories,
			List<String> failureMessages,
			DomainEventPublisher<PaymentCompletedEvent> publisher,
			DomainEventPublisher<PaymentFailedEvent> publisherFailedEvent);

	PaymentEvent validateAndCancelPayment(Payment payment,
			CreditEntry creditEntry,
			List<CreditHistory> creditHistories,
			List<String> failureMessages,
			DomainEventPublisher<PaymentCancelledEvent> publisher,
			DomainEventPublisher<PaymentFailedEvent> publisherFailedEvent);

}
