package com.food.ordering.system.payment.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;
	private final PaymentRequestMessageListener paymentRequestMessageListener;

	public PaymentRequestKafkaListener(PaymentMessagingDataMapper paymentMessagingDataMapper,
			PaymentRequestMessageListener paymentRequestMessageListener) {
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.paymentRequestMessageListener = paymentRequestMessageListener;
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${payment-service.payment-request-topic-name}")
	public void recieve(@Payload List<PaymentRequestAvroModel> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		log.info("{} number of payment request recieved with keys: {}, partitions: {}, and offsets: {}",
				messages.size(),
				keys.toString(),
				partitions.toString(),
				offsets.toString());

		messages.forEach(message -> {
			if (PaymentOrderStatus.CANCELLED == message.getPaymentOrderStatus()) {
				paymentRequestMessageListener
						.cancelPayment(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message));
			} else if (PaymentOrderStatus.PENDING == message.getPaymentOrderStatus()) {
				paymentRequestMessageListener
						.completePayment(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message));
			} else {
				log.error("Invalid payment order status recieved: {}", message.getPaymentOrderStatus());
			}
		});

	}

}
