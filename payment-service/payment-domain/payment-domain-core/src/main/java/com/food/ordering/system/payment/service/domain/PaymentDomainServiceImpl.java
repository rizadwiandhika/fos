package com.food.ordering.system.payment.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

	@Override
	public PaymentEvent validateAndInitiatePayment(Payment payment, CreditEntry creditEntry,
			List<CreditHistory> creditHistories, List<String> failureMessages) {
		payment.validatePayment(failureMessages);
		payment.initializePayment();

		validateCreditEntry(payment, creditEntry, failureMessages);
		substractCreditEntry(payment, creditEntry);
		updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
		validateCreditHistory(creditEntry, creditHistories, failureMessages);

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(DomainConstants.UTC));

		if (failureMessages.isEmpty()) {
			log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
			payment.updateStatus(PaymentStatus.COMPLETED);
			return new PaymentCompletedEvent(payment, now);
		}

		log.info("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
		payment.updateStatus(PaymentStatus.FAILED);

		return new PaymentFailedEvent(payment, now, failureMessages);
	}

	@Override
	public PaymentEvent validateAndCancelPayment(Payment payment, CreditEntry creditEntry,
			List<CreditHistory> creditHistories, List<String> failureMessages) {

		payment.validatePayment(failureMessages);

		addCreditEntry(payment, creditEntry);
		updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(DomainConstants.UTC));

		if (failureMessages.isEmpty()) {
			log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
			payment.updateStatus(PaymentStatus.CANCELLED);
			return new PaymentCancelledEvent(payment, now);
		}

		log.info("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
		payment.updateStatus(PaymentStatus.FAILED);

		return new PaymentFailedEvent(payment, now, failureMessages);
	}

	private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
		creditEntry.addCredit(payment.getPrice());
	}

	private void validateCreditHistory(CreditEntry creditEntry, List<CreditHistory> creditHistories,
			List<String> failureMessages) {
		Money totalCreditHistory = getTotalHistoryByType(creditHistories, TransactionType.CREDIT);
		Money totalDebitHistory = getTotalHistoryByType(creditHistories, TransactionType.DEBIT);

		if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
			log.error("Customer id: {} doesn't have enough credit according to credit history",
					creditEntry.getCustomerId().getValue());
			failureMessages.add("Customer id: " + creditEntry.getCustomerId().getValue().toString()
					+ " doesn't have enough credit according to credit history");
		}

		if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.substract(totalDebitHistory))) {
			log.error("Credit history total is not equal to current credit for customer id: {}",
					creditEntry.getCustomerId().getValue());
			failureMessages.add("Customer id: " + creditEntry.getCustomerId().getValue().toString()
					+ " Credit history total is not equal to current credit");
		}
	}

	private Money getTotalHistoryByType(List<CreditHistory> creditHistories, TransactionType type) {
		return creditHistories.stream()
				.filter(creditHistory -> creditHistory.getTransactionType() == type)
				.map(CreditHistory::getAmount)
				.reduce(Money.ZERO, Money::add);
	}

	private void updateCreditHistory(Payment payment, List<CreditHistory> creditHistories, TransactionType type) {
		CreditHistory creditHistory = CreditHistory.builder()
				.creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
				.customerId(payment.getCustomerId())
				.amount(payment.getPrice())
				.transactionType(type)
				.build();

		creditHistories.add(creditHistory);
	}

	private void substractCreditEntry(Payment payment, CreditEntry creditEntry) {
		creditEntry.substractCredit(payment.getPrice());
	}

	private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
		if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
			log.error("Customer id: {} doesn't have enough credit for payment", payment.getCustomerId().getValue());
			failureMessages.add("Customer id: " + payment.getCustomerId().getValue().toString()
					+ " doesn't have enough credit for payment");
		}
	}

}
