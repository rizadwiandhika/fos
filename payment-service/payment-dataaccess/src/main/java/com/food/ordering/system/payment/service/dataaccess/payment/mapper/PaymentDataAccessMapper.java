package com.food.ordering.system.payment.service.dataaccess.payment.mapper;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.payment.service.dataaccess.payment.entity.PaymentEntity;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataAccessMapper {

    public PaymentEntity paymentToPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId().getValue())
                .customerId(payment.getCustomerId().getValue())
                .orderId(payment.getOrderId().getValue())
                .price(payment.getPrice().getAmount())
                .status(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public Payment paymentEntityToPayment(PaymentEntity paymentEntity) {
        return Payment.builder()
                .withPaymentId(new PaymentId(paymentEntity.getId()))
                .withCustomerId(new CustomerId(paymentEntity.getCustomerId()))
                .withOrderId(new OrderId(paymentEntity.getOrderId()))
                .withPrice(new Money(paymentEntity.getPrice()))
                .withCreatedAt(paymentEntity.getCreatedAt())
                .build();
    }

}
