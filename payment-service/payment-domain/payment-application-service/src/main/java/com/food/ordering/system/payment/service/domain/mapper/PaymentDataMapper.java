package com.food.ordering.system.payment.service.domain.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;

@Component
public class PaymentDataMapper {

	public Payment paymentRequestToPayment(PaymentRequest paymentRequest) {
		return Payment.builder()
				.withCustomerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
				.withPrice(new Money(paymentRequest.getPrice()))
				.build();
	}

}
